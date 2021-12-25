package com.dalti.laposte.core.model;

import android.app.Activity;
import android.app.Application;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.BooleanSetting;
import com.dalti.laposte.core.repositories.Event;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.InputProperty;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.StringPreference;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.ui.PhoneService;
import com.dalti.laposte.core.util.QueueConfig;
import com.dalti.laposte.core.util.QueueUtils;
import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.BasicJob;
import dz.jsoftware95.silverbox.android.concurrent.DuoJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;
import dz.jsoftware95.silverbox.android.frontend.LiveMappedRepresentation;
import dz.jsoftware95.silverbox.android.frontend.Representable;
import dz.jsoftware95.silverbox.android.middleware.BasicModel;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

@MainThread
public abstract class PhoneModel extends BasicModel implements PhoneService {
    protected static final String LAST_SMS_VERIFICATION_TIME = PhoneModel.class.getName() + "#LAST_SMS_VERIFICATION_TIME";

    protected final ExtraRepository extraRepository;

    @Nullable
    protected VerificationContext verificationContext = null;
    @Nullable
    protected PhoneService.VerificationCodeCallback currentCallback = null;

    protected final MutableLiveData<State> state;
    protected final MutableLiveData<String> stateRepresentation;
    protected final LiveData<Integer> codeInputVisibility;

    public PhoneModel(@NonNull Application application,
                      @NonNull ExtraRepository extraRepository) {
        super(application);
        this.extraRepository = extraRepository;
        state = new MutableLiveData<>(null);
        stateRepresentation = new LiveMappedRepresentation(application.getResources(), state);
        codeInputVisibility = Transformations.map(state, currentState ->
                (currentState == State.WAITING || currentState == State.TIMEOUT) ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE);
    }

    @MainThread
    protected void setState(State newState) {
        Teller.log(Event.SmsVerificationStateUpdated.NAME, Event.SmsVerificationStateUpdated.Param.UPDATED_SMS_VERIFICATION_STATE, String.valueOf(newState));
        state.setValue(newState);
    }

    @AnyThread
    protected void postState(State newState) {
        Teller.log(Event.SmsVerificationStateUpdated.NAME, Event.SmsVerificationStateUpdated.Param.UPDATED_SMS_VERIFICATION_STATE, String.valueOf(newState));
        state.postValue(newState);
    }

    public <T extends Activity & VerificationCodeCallback> boolean savePhone(@NonNull T activity, TextView input, StringPreference preference) {
        Objects.requireNonNull(activity);
        input.setError(null);
        String phoneInput = StringUtil.getString(input);
        AppConfig appConfig = AppConfig.getInstance();
        if (StringUtil.isNullOrEmpty(phoneInput)) {
            if (!StringUtil.isNullOrEmpty(appConfig.getString(preference.name()))) {
                extraRepository.resetString(preference);
                resetState(R.string.phone_number_removed);
                return false;
            } else {
                input.setError(activity.getString(R.string.input_required));
                resetState(null);
                return true;
            }
        } else {
            switch (InputProperty.validatePhoneInput(phoneInput)) {
                case TOO_SHORT:
                    input.setError(activity.getString(R.string.input_too_short));
                    resetState(null);
                    return true;
                case TOO_LONG:
                    input.setError(activity.getString(R.string.input_too_long));
                    resetState(null);
                    return true;
                case INVALID:
                    input.setError(activity.getString(R.string.invalid_phone));
                    resetState(null);
                    return true;
                default:
                    String phonePrefix = activity.getString(R.string.phone_prefix);
                    String phone = phonePrefix + phoneInput;
                    if (!phone.equals(appConfig.getString(preference.name())))
                        verifyPhone(phone, preference);
                    else
                        resetState(R.string.phone_number_up_to_date);

                    return false;
            }
        }
    }

    @AnyThread
    protected void resetState(Integer toast) {
        postState(null);
        if (toast != null)
            QueueUtils.toast(toast);
    }

    protected void verificationSuccessful() {
        VerificationCodeCallback currentCallback = this.currentCallback;
        if (currentCallback != null)
            currentCallback.verificationSuccessful();
    }

    public <T extends Activity & VerificationCodeCallback> void verifyPhone(String phone, StringPreference preference) {
        State currentState = state.getValue();
        VerificationContext context = verificationContext;

        if (AppConfig.getInstance().getRemoteBoolean(BooleanSetting.ENABLE_FIREBASE_SMS_AUTH)) {
            if (currentState != null && currentState.isActive())
                QueueUtils.toast(R.string.wait_active_check);
            else if (isPhoneVerificationOnGoing())
                QueueUtils.toast(R.string.wait_active_request);
            else
                startPhoneVerification(newVerificationContext(phone, preference));
        } else
            onComplete(true, phone, preference);
    }

    public void onComplete(boolean successful, String phone, StringPreference preference) {
        if (successful) {
            extraRepository.setString(preference, phone);
            onUpdate(preference, phone);
            resetState(R.string.phone_number_updated);
            verificationSuccessful();
        } else
            setState(State.CODE_CHECK_FAILED);
    }

    public boolean isPhoneVerificationOnGoing() {
        return extraRepository.getLongStore().containsKey(LAST_SMS_VERIFICATION_TIME);
    }

    public void startPhoneVerification(VerificationContext context) {
        if (currentCallback != null)
            verifyPhone(context, currentCallback);
    }

    public void verifyPhone(@NonNull VerificationContext context,
                            @NonNull PhoneService.VerificationCodeCallback callback) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(AbstractQueueApplication.requireInstance().getFirebaseAuth())
                .setPhoneNumber(context.getPhone())
                .setTimeout(AppConfig.getInstance().getRemoteLong(LongSetting.SMS_TIMEOUT), TimeUnit.SECONDS)
                .setActivity(callback.getActivity())
                .setCallbacks(context)
                .build();
        context.markVerificationStarted();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void cancelVerificationAfterTimeout(long epoch, long timeout) {
        new BasicJob() {
            @Override
            protected void doFromMain() {
                if (extraRepository.getLongStore().remove(LAST_SMS_VERIFICATION_TIME, epoch)) {
                    cancelVerification();
                }
            }
        }.executeDelayed(timeout);
    }

    public void cancelVerification() {
        VerificationContext verificationContext = this.verificationContext;
        if (verificationContext != null)
            verificationContext.close();
        VerificationCodeCallback callback = this.currentCallback;
        if (callback != null)
            callback.hideVerificationCodeDialog();
    }

    @MainThread
    public void enterVerificationCode(String code) {
        VerificationContext context = this.verificationContext;
        if (context != null) {
            String verificationId = context.getVerificationId();
            if (verificationId != null)
                authenticate(context, PhoneAuthProvider.getCredential(verificationId, code));
        }
    }

    @AnyThread
    protected void authenticate(VerificationContext context, PhoneAuthCredential credential) {
        markAsCheckingJob(this, credential).execute();
    }

    public void setAsCurrentCallback(@NonNull PhoneService.VerificationCodeCallback newCallback) {
        VerificationCodeCallback oldCallback = this.currentCallback;
        if (oldCallback == newCallback)
            return;
        if (oldCallback != null)
            oldCallback.hideVerificationCodeDialog();

        this.currentCallback = newCallback;

        if (isWaitingForVerificationCode())
            newCallback.showVerificationCodeDialog();

        VerificationContext context = this.verificationContext;
        if (context != null && isPhoneVerificationOnGoing())
            startPhoneVerification(context);
    }

    public void removeCurrentCallback() {
        VerificationCodeCallback currentCallback = this.currentCallback;
        if (currentCallback != null) {
            currentCallback.hideVerificationCodeDialog();
            this.currentCallback = null;
        }
    }

    protected static long getMaxWaitDuration() {
        return TimeUnit.SECONDS.toMillis(AppConfig.getInstance().getRemoteLong(LongSetting.SMS_TIMEOUT) + QueueConfig.ANR_TIMEOUT_SECONDS);
    }

    @AnyThread
    protected static Job markAsCheckingJob(PhoneModel phoneModel, PhoneAuthCredential credential) {
        return new DuoJob<PhoneModel, PhoneAuthCredential>(phoneModel, credential) {
            @Override
            protected void doFromMain(@NonNull PhoneModel context,
                                      @NonNull PhoneAuthCredential credential) {
                context.setState(State.CHECKING);
                VerificationCodeCallback currentCallback = context.currentCallback;
                if (currentCallback != null)
                    currentCallback.hideVerificationCodeDialog();

                FirebaseAuth auth = AbstractQueueApplication.requireInstance().getFirebaseAuth();
                auth.signOut();
                Task<AuthResult> authTask = auth.signInWithCredential(credential);
                if (context.verificationContext != null)
                    authTask.addOnCompleteListener(context.verificationContext);
            }
        };
    }

    @AnyThread
    protected static Job markAsWaitingJob(PhoneModel phoneModel) {
        return new UnJob<PhoneModel>(phoneModel) {
            @Override
            protected void doFromMain(@NonNull PhoneModel context) {
                VerificationCodeCallback currentCallback = context.currentCallback;
                if (currentCallback != null)
                    currentCallback.showVerificationCodeDialog();
                context.setState(State.WAITING);
            }
        };
    }

    public boolean isWaitingForVerificationCode() {
        return state.getValue() == State.WAITING;
    }

    public VerificationContext newVerificationContext(String phone, StringPreference preference) {
        VerificationContext verificationContext = this.verificationContext;
        if (verificationContext != null)
            verificationContext.close();

        return this.verificationContext = new VerificationContext(this, phone, preference);
    }

    public LiveData<String> getStateRepresentation() {
        return stateRepresentation;
    }

    public LiveData<Integer> getCodeInputVisibility() {
        return codeInputVisibility;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (verificationContext != null)
            verificationContext.close();
    }

    public enum State implements Representable {
        SENDING(R.string.sending_sms, true),
        WAITING(R.string.waiting_sms, true),
        CHECKING(R.string.checking_code, true),

        TIMEOUT(R.string.sms_timeout),
        NETWORK_ERROR(R.string.network_error),
        BAD_PHONE_NUMBER(R.string.bad_phone_number),
        TOO_MANY_REQUESTS(R.string.too_many_request),
        CODE_CHECK_FAILED(R.string.verification_code_check_failed),
        FAILED(R.string.sms_verification_failed),
        ;
        private final int messageRes;
        private final boolean active;

        State(@StringRes int messageRes) {
            this(messageRes, false);
        }

        State(@StringRes int messageRes, boolean active) {
            this.messageRes = messageRes;
            this.active = active;
        }

        @Override
        public int getStringRes() {
            return messageRes;
        }

        public boolean isActive() {
            return active;
        }
    }

    @AnyThread
    public static class VerificationContext extends PhoneAuthProvider.OnVerificationStateChangedCallbacks
            implements OnCompleteListener<AuthResult> {

        @Nullable
        volatile PhoneModel model;
        final String phone;
        final StringPreference preference;
        volatile String verificationId;

        public VerificationContext(@NonNull PhoneModel model,
                                   @NonNull String phone,
                                   @NonNull StringPreference preference) {
            this.model = Assert.nonNull(model);
            this.phone = Assert.nonNull(phone);
            this.preference = Assert.nonNull(preference);
        }

        @MainThread
        public void markVerificationStarted() {
            long now = System.currentTimeMillis();
            PhoneModel model = this.model;
            if (model != null) {
                model.postState(State.SENDING);
                model.extraRepository.getLongStore().put(LAST_SMS_VERIFICATION_TIME, now);
                model.cancelVerificationAfterTimeout(now, getMaxWaitDuration());
            }
        }

        public void markVerificationEnded() {
            PhoneModel model = this.model;
            if (model != null) {
                model.extraRepository.getLongStore().remove(LAST_SMS_VERIFICATION_TIME);
            }
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            PhoneModel model = this.model;
            Teller.info("onVerificationCompleted() with model: " + (model != null));

            markVerificationEnded();
            if (model != null)
                model.authenticate(this, phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            PhoneModel model = this.model;
            Teller.warn("onVerificationFailed() with model: " + (model != null), e);

            markVerificationEnded();
            if (model != null) {
                if (e instanceof FirebaseTooManyRequestsException) {
                    model.postState(State.TOO_MANY_REQUESTS);
                } else if (e instanceof FirebaseNetworkException) {
                    model.postState(State.NETWORK_ERROR);
                } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    model.postState(State.BAD_PHONE_NUMBER);
                } else {
                    model.postState(State.FAILED);
                }
            }
        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken resendingToken) {
            PhoneModel model = this.model;
            Teller.info("onCodeSent() called with model: " + (model != null));

            if (model != null) {
                this.verificationId = verificationId;
                markAsWaitingJob(model).execute();
            }
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String verificationId) {
            Teller.info("onCodeAutoRetrievalTimeOut() called with model: " + (model != null));
            markVerificationEnded();
        }

        @Override
        @MainThread
        public void onComplete(@NonNull Task<AuthResult> task) {
            PhoneModel model = this.model;
            final boolean successful = task.isSuccessful();
            Teller.info("onComplete() called with model: " + (model != null) + " isSuccessful: " + successful);

            if (model != null)
                model.onComplete(successful, phone, preference);
        }

        public String getPhone() {
            return phone;
        }

        public String getVerificationId() {
            return verificationId;
        }

        public void resetState() {
            PhoneModel model = this.model;
            if (model != null)
                model.postState(null);
        }

        public void close() {
            resetState();
            this.model = null;
        }
    }

    protected void onUpdate(StringPreference preference, String phone) {
    }
}
