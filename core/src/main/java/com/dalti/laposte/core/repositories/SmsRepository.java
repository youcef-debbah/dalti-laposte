package com.dalti.laposte.core.repositories;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsManager;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.hilt.work.HiltWorker;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dalti.laposte.core.entity.CoreAPI;
import com.dalti.laposte.core.entity.ShortMessage;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.ui.Request;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dz.jsoftware95.queue.common.Function;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.common.SmsState;
import dz.jsoftware95.queue.api.ServerResponse;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.backend.LiveListRepository;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.DuoDatabaseJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import retrofit2.Call;

@Singleton
@AnyThread
public class SmsRepository extends LiveListRepository<ShortMessage, ShortMessageDAO> {

    public static final String SMS_TOKEN = "SMS_TOKEN";
    public static final String SMS_STATE = "SMS_STATE";

    public static final String SMS_PHONE = "SMS_PHONE";
    public static final String SMS_TEXT_CONTENT = "SMS_TEXT_CONTENT";
    public static final String SMS_SUPPORT_RESENDING = "SMS_SUPPORT_RESENDING";
    public static final String APP_ID = "APP_ID";
    public static final String SMS_ID = "SMS_ID";

    public static final int RESEND_DELAY_MINUTES = 2;
    public static final int RESEND_INTERVAL_MINUTES = 3;
    public static final String[] SMS_SENDING_PERMISSIONS = {Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE};

    private final Lazy<CoreAPI> coreAPI;

    private final MutableLiveData<Boolean> smsSendingEnabled = new MutableLiveData<>(AppConfig.getInstance().get(BooleanSetting.SMS_REQUESTS_ENABLED));
    private final LiveLongProperty smsRequestsCounter = new LiveLongProperty(LongSetting.SMS_REQUESTED);
    private final LiveLongProperty smsDeliveredCounter = new LiveLongProperty(LongSetting.SMS_DELIVERED);
    private final LiveStringProperty smsLatestOutcome = new LiveStringProperty(StringSetting.SMS_LATEST_OUTCOME);
    private final LiveLongProperty smsIgnoredCounter = new LiveLongProperty(LongSetting.SMS_IGNORED);
    private final LiveLongProperty smsSentCounter = new LiveLongProperty(LongSetting.SMS_SENT);
    private final LiveLongProperty smsFailedCounter = new LiveLongProperty(LongSetting.SMS_FAILED);

    @Inject
    public SmsRepository(Lazy<ShortMessageDAO> shortMessageDAO, Lazy<CoreAPI> coreAPI) {
        super(shortMessageDAO);
        this.coreAPI = coreAPI;
    }

    @MainThread
    public void enableShortMessagesRequests() {
        AppConfig.getInstance().put(BooleanSetting.SMS_REQUESTS_ENABLED, true);
        smsSendingEnabled.setValue(true);
    }

    @MainThread
    public void disableShortMessagesRequests() {
        AppConfig.getInstance().put(BooleanSetting.SMS_REQUESTS_ENABLED, false);
        smsSendingEnabled.setValue(false);
    }

    @MainThread
    public void testSMS(String phoneNumber) {
        String textContent = "(Test) Dalti-laposte, Billet en cours: 999, Votre billet: 999, Temps restant: environ 90 minutes, Poste Centrale de Ouargla";
        handleSmsRequest(phoneNumber, textContent, UUID.randomUUID().toString(), true);
    }

    @MainThread
    public void handleSmsRequest(String phone, String textContent, String smsToken, boolean resend) {
        smsRequestsCounter.increment();
        try {
            AbstractQueueApplication context = AbstractQueueApplication.getInstance();
            if (context != null && textContent != null && phone != null) {
                if (AppConfig.getInstance().get(BooleanSetting.SMS_REQUESTS_ENABLED))
                    if (ContextUtils.isPermissionsGranted(context, SMS_SENDING_PERMISSIONS)) {
                        SimStrategy simStrategy = new SimStrategy(context, phone);
                        if (simStrategy.isDeliverable()) {
                            sendSms(context, simStrategy, textContent, smsToken, resend);
                            return;
                        }
                    } else {
                        disableShortMessagesRequests();
                        QueueUtils.postToast("SMS sending disabled (permission missing)");
                        Teller.logPermissionsDenied(context, SMS_SENDING_PERMISSIONS);
                    }
            } else
                Teller.logUnexpectedNull(context, textContent, phone);
        } catch (RuntimeException e) {
            Teller.warn("could not send sms: '" + textContent + "' to: " + phone, e);
            confirmSmsSkip(Item.AUTO_ID, smsToken, ShortMessage.LOCAL_ERROR);
        }
        smsIgnoredCounter.increment();
    }

    @AnyThread
    public void postSmsRequest(Map<String, String> data) {
        String phone = StringUtil.getString(data, GlobalConf.SMS_REQUEST_PHONE);
        String textContent = StringUtil.getString(data, GlobalConf.SMS_REQUEST_TEXT_CONTENT);
        String smsToken = StringUtil.getString(data, GlobalConf.SMS_REQUEST_TOKEN);
        postSmsRequest(phone, textContent, smsToken, true);
    }

    @AnyThread
    public void postSmsRequest(String phone, String textContent, String smsToken, boolean resend) {
        newPostSmsRequestJob(this, phone, textContent, smsToken, resend).execute();
    }

    @NotNull
    public static UnJob<SmsRepository> newPostSmsRequestJob(SmsRepository repository,
                                                            String phone, String textContent,
                                                            String smsToken, boolean resend) {
        return new UnJob<SmsRepository>(repository) {
            @Override
            protected void doFromMain(@NonNull SmsRepository repository) {
                repository.handleSmsRequest(phone, textContent, smsToken, resend);
            }
        };
    }

    public LiveLongProperty getSmsSentCounter() {
        return smsSentCounter;
    }

    public LiveLongProperty getSmsDeliveredCounter() {
        return smsDeliveredCounter;
    }

    public void countDelivered(long id, String smsToken) {
        execute(newMarkMessageAsDeliveredJob(this, id));
        smsDeliveredCounter.increment();
        Teller.logDeliveredSms(id, smsToken);
    }

    private static Job newMarkMessageAsDeliveredJob(SmsRepository smsRepository, long id) {
        long now = System.currentTimeMillis();
        return new UnJob<SmsRepository>(AppWorker.LOG, smsRepository) {
            @Override
            protected void doFromBackground(@NonNull SmsRepository context) {
                context.requireDAO().markAsDelivered(id, now);
            }
        };
    }

    public LiveStringProperty getSmsLatestOutcome() {
        return smsLatestOutcome;
    }


    public LiveLongProperty getSmsIgnoredCounter() {
        return smsIgnoredCounter;
    }

    public LiveLongProperty getSmsRequestsCounter() {
        return smsRequestsCounter;
    }

    public LiveLongProperty getSmsFailedCounter() {
        return smsFailedCounter;
    }

    public void resetCounters() {
        smsRequestsCounter.reset();
        smsDeliveredCounter.reset();
        smsLatestOutcome.reset();
        smsIgnoredCounter.reset();
        smsSentCounter.reset();
        smsFailedCounter.reset();
    }

    public LiveData<Boolean> getSmsSendingEnabled() {
        return smsSendingEnabled;
    }

    @MainThread
    @SuppressLint("MissingPermission")
    private void sendSms(AbstractQueueApplication context, SimStrategy simStrategy,
                         String textContent, String smsToken, boolean resend) {
        SmsManager smsManager = getSmsManager(context, simStrategy.getSubscription());
        Teller.info("£££ " + simStrategy);
        ArrayList<String> parts = smsManager.divideMessage(textContent);
        long smsID = GlobalUtil.randomLong();

        Intent sent = context.newActionReceiverIntent(Request.COUNT_SMS_AS_SENT.name());
        sent.putExtra(SMS_SUPPORT_RESENDING, resend);
        sent.putExtra(SMS_TOKEN, smsToken);
        sent.putExtra(SMS_PHONE, simStrategy.getPhone());
        sent.putExtra(SMS_TEXT_CONTENT, textContent);
        sent.putExtra(SMS_ID, smsID);
        PendingIntent sentIntent = ContextUtils.getBroadcastIntent(context, Request.COUNT_SMS_AS_SENT.ordinal(), sent);
        ArrayList<PendingIntent> sentIntents = new ArrayList<>(1);
        sentIntents.add(sentIntent);

        Intent delivered = context.newActionReceiverIntent(Request.COUNT_SMS_AS_DELIVERED.name());
        delivered.putExtra(SMS_TOKEN, smsToken);
        delivered.putExtra(SMS_ID, smsID);
        PendingIntent deliveredIntent = ContextUtils.getBroadcastIntent(context, Request.COUNT_SMS_AS_DELIVERED.ordinal(), delivered);
        ArrayList<PendingIntent> deliveredIntents = new ArrayList<>(1);
        deliveredIntents.add(deliveredIntent);

        execute(newSaveMessageJob(this, smsID, simStrategy.getPhone(), smsToken, textContent, GlobalUtil.size(parts)));
        smsManager.sendMultipartTextMessage(simStrategy.getPhone(), null, parts, sentIntents, deliveredIntents);
    }

    @NotNull
    private static UnJob<SmsRepository> newSaveMessageJob(SmsRepository repository, long id, String phone, String smsToken, String textContent, int parts) {
        long now = System.currentTimeMillis();
        return new UnJob<SmsRepository>(AppWorker.LOG, repository) {
            @Override
            protected void doFromBackground(@NonNull SmsRepository repository) {
                repository.requireDAO().save(new ShortMessage(id, now, phone, smsToken, textContent, parts));
            }
        };
    }

    public static SmsManager getSmsManager(Context context, @Nullable Integer subId) {
        if (subId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                return context.getSystemService(SmsManager.class).createForSubscriptionId(subId);
            //noinspection deprecation
            return SmsManager.getSmsManagerForSubscriptionId(subId);
        } else
            //noinspection deprecation
            return SmsManager.getDefault();
    }

    @WorkerThread
    private void cancelSmsNow(String smsToken, int state) {
        if (StringUtil.notBlank(smsToken))
            try {
                Call<ServerResponse> call = coreAPI.get().cancelSMS(smsToken, state, AppConfig.getInstance().getActivationKey());
                if (!call.execute().isSuccessful())
                    Teller.info("unsuccessful cancel sms call: " + smsToken);
            } catch (Exception e) {
                Teller.warn("could not cancel SMS: " + smsToken, e);
            }
    }

    @WorkerThread
    private void skipSmsNow(String smsToken, String appID) {
        if (StringUtil.notBlank(smsToken) && StringUtil.notBlank(appID)) {
            try {
                Call<ServerResponse> call = coreAPI.get().skipOperator(smsToken, appID, AppConfig.getInstance().getActivationKey());
                if (!call.execute().isSuccessful())
                    Teller.warn("unsuccessful skip sms call: " + smsToken);
            } catch (Exception e) {
                Teller.warn("could not skip sms: " + smsToken, e);
            }
        }
    }

    public void confirmSmsSentSuccessfully(long id, String smsToken) {
        execute(newMarkMessageAsConfirmedJob(this, id, ShortMessage.OK_STATE));
        cancelSMS(smsToken, SmsState.SENT_SUCCESSFULLY);
        smsSentCounter.increment();
        smsLatestOutcome.set(SmsRepository.getOutcome(ShortMessage.OK_STATE));
        Teller.logSendSmsSucceed(id, smsToken);
    }

    public void confirmSmsSkip(long id, String smsToken, int resultCode) {
        execute(newMarkMessageAsConfirmedJob(this, id, resultCode));
        skipSMS(smsToken, AppConfig.getInstance().getApplicationID());
        smsFailedCounter.increment();
        smsLatestOutcome.set(SmsRepository.getOutcome(resultCode));
        Teller.logSendSmsFailure(id, smsToken, resultCode);
    }

    private static Job newMarkMessageAsConfirmedJob(SmsRepository smsRepository, long id, int resultCode) {
        long now = System.currentTimeMillis();
        return new UnJob<SmsRepository>(AppWorker.LOG, smsRepository) {
            @Override
            protected void doFromBackground(@NonNull SmsRepository context) {
                context.requireDAO().markAsConfirmed(id, now, resultCode);
            }
        };
    }

    public static void cancelByClientConfirmation(String smsToken) {
        cancelSMS(smsToken, SmsState.CANCELED_BY_CLIENT_CONFIRMATION);
    }

    public static void scheduleResend(String smsToken, String phone, String textContent) {
        if (StringUtil.notBlank(smsToken) && phone != null && textContent != null) {
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ResendWorker.class)
                    .setInitialDelay(RESEND_DELAY_MINUTES, TimeUnit.MINUTES)
                    .setInputData(Teller.logWorkerRequest(ResendWorker.NAME)
                            .putString(SMS_PHONE, phone)
                            .putString(SMS_TEXT_CONTENT, textContent)
                            .putString(SMS_TOKEN, smsToken)
                            .build())
                    .build();

            String workName = ResendWorker.NAME + smsToken;
            AbstractQueueApplication.getWorkManager().enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request);
            AbstractQueueApplication.cancelWork(workName, RESEND_DELAY_MINUTES + RESEND_INTERVAL_MINUTES, TimeUnit.MINUTES);
        }
    }

    public static void cancelResend(String smsToken) {
        if (StringUtil.notBlank(smsToken))
            AbstractQueueApplication.cancelWork(ResendWorker.NAME + smsToken);
    }

    private static void cancelSMS(String smsToken, int state) {
        if (StringUtil.notBlank(smsToken)) {
            String workName = CancelSmsWorker.NAME + smsToken;
            AbstractQueueApplication.enqueueUniqueNetworkJob(workName, CancelSmsWorker.class,
                    Teller.logWorkerRequest(CancelSmsWorker.NAME)
                            .putString(SMS_TOKEN, smsToken)
                            .putInt(SMS_STATE, state)
                            .build());
            AbstractQueueApplication.cancelWork(workName, SmsState.SENDING_SMS_TIMEOUT * 2, TimeUnit.MILLISECONDS);
        }
    }

    private static void skipSMS(String smsToken, String appID) {
        if (StringUtil.notBlank(smsToken) && StringUtil.notBlank(appID)) {
            String workName = SkipSmsWorker.NAME + smsToken;
            AbstractQueueApplication.enqueueUniqueNetworkJob(workName, SkipSmsWorker.class,
                    Teller.logWorkerRequest(SkipSmsWorker.NAME)
                            .putString(SMS_TOKEN, smsToken)
                            .putString(APP_ID, appID)
                            .build());
            AbstractQueueApplication.cancelWork(workName, SmsState.SENDING_SMS_TIMEOUT * 2, TimeUnit.MILLISECONDS);
        }
    }

    public LiveData<Integer> countMessages() {
        return countMessagesEntities(ShortMessageDAO::countMessages);
    }

    public LiveData<Integer> countOoredooMessages() {
        return countMessagesEntities(ShortMessageDAO::countOoredooMessages);
    }

    public LiveData<Integer> countMobilisMessages() {
        return countMessagesEntities(ShortMessageDAO::countMobilisMessages);
    }

    public LiveData<Integer> countDjezzyMessages() {
        return countMessagesEntities(ShortMessageDAO::countDjezzyMessages);
    }

    public LiveData<Integer> countMessages(int code) {
        return countMessagesEntities((ShortMessageDAO dao) -> dao.countMessages(code));
    }

    public LiveData<Integer> countOoredooMessages(int code) {
        return countMessagesEntities((ShortMessageDAO dao) -> dao.countOoredooMessages(code));
    }

    public LiveData<Integer> countMobilisMessages(int code) {
        return countMessagesEntities((ShortMessageDAO dao) -> dao.countMobilisMessages(code));
    }

    public LiveData<Integer> countDjezzyMessages(int code) {
        return countMessagesEntities((ShortMessageDAO dao) -> dao.countDjezzyMessages(code));
    }

    private LiveData<Integer> countMessagesEntities(Function<ShortMessageDAO, Integer> daoCall) {
        MutableLiveData<Integer> data = new MutableLiveData<>(null);
        execute(newCountMessagesJob(this, daoCall, data));
        return data;
    }

    private static Job newCountMessagesJob(SmsRepository smsRepository, Function<ShortMessageDAO, Integer> daoCall, MutableLiveData<Integer> data) {
        return new DuoDatabaseJob<SmsRepository, Function<ShortMessageDAO, Integer>>(smsRepository, daoCall) {
            @Override
            protected void doFromBackground(@NonNull SmsRepository repository, @NotNull Function<ShortMessageDAO, Integer> daoCall) {
                data.postValue(daoCall.apply(repository.requireDAO()));
            }
        };
    }

    @HiltWorker
    public static class CancelSmsWorker extends Worker {

        public static final String NAME = "cancel_sms_worker";

        private final SmsRepository smsRepository;

        @AssistedInject
        public CancelSmsWorker(@Assisted @NotNull Context context,
                               @Assisted @NotNull WorkerParameters workerParams,
                               SmsRepository smsRepository) {
            super(context, workerParams);
            this.smsRepository = smsRepository;
        }

        @NonNull
        @NotNull
        @Override
        public Result doWork() {
            Data data = getInputData();
            Teller.logWorkerSession(data);
            smsRepository.cancelSmsNow(data.getString(SMS_TOKEN), data.getInt(SMS_STATE, 0));
            return Result.success();
        }
    }

    @HiltWorker
    public static class SkipSmsWorker extends Worker {

        public static final String NAME = "skip_sms_worker";

        private final SmsRepository smsRepository;

        @AssistedInject
        public SkipSmsWorker(@Assisted @NotNull Context context,
                             @Assisted @NotNull WorkerParameters workerParams,
                             SmsRepository smsRepository) {
            super(context, workerParams);
            this.smsRepository = smsRepository;
        }

        @NonNull
        @NotNull
        @Override
        public Result doWork() {
            Data data = getInputData();
            Teller.logWorkerSession(data);
            smsRepository.skipSmsNow(data.getString(SMS_TOKEN), data.getString(APP_ID));
            return Result.success();
        }
    }

    @HiltWorker
    public static class ResendWorker extends Worker {

        public static final String NAME = "resend_worker";

        private final SmsRepository smsRepository;

        @AssistedInject
        public ResendWorker(@Assisted @NotNull Context context,
                            @Assisted @NotNull WorkerParameters workerParams,
                            SmsRepository smsRepository) {
            super(context, workerParams);
            this.smsRepository = smsRepository;
        }

        @NonNull
        @NotNull
        @Override
        public Result doWork() {
            Data data = getInputData();
            Teller.logWorkerSession(data);

            final String phone = data.getString(SMS_PHONE);
            final String content = data.getString(SMS_TEXT_CONTENT);
            final String smsToken = data.getString(SMS_TOKEN);
            smsRepository.postSmsRequest(phone, content, smsToken, false);

            return Result.success();
        }

    }

    public static String getOutcome(int code) {
        switch (code) {
            case ShortMessage.LOCAL_ERROR:
                return "LOCAL ERROR";

            case ShortMessage.OK_STATE:
                return "OK";

            case ShortMessage.NULL_STATE:
                return "N/A";

            case ShortMessage.SENDING_STATE:
                return "SENDING...";

            /* Generic failure cause */
            case 1:
                return "ERROR GENERIC FAILURE";

            /* Failed because radio was explicitly turned off */
            case 2:
                return "ERROR RADIO OFF";

            /* Failed because no pdu provided */
            case 3:
                return "ERROR NULL PDU";

            /* Failed because service is currently unavailable */
            case 4:
                return "ERROR NO SERVICE";

            /* Failed because we reached the sending queue limit. */
            case 5:
                return "ERROR LIMIT EXCEEDED";

            /*
             * Failed because FDN is enabled.
             */
            case 6:
                return "ERROR FDN CHECK FAILURE";

            /* Failed because user denied the sending of this short code. */
            case 7:
                return "ERROR SHORT CODE NOT ALLOWED";

            /* Failed because the user has denied this app ever send premium short codes. */
            case 8:
                return "ERROR SHORT CODE NEVER ALLOWED";

            /*
             * Failed because the radio was not available
             */
            case 9:
                return "RADIO NOT AVAILABLE";

            /*
             * Failed because of network rejection
             */
            case 10:
                return "NETWORK REJECT";

            /*
             * Failed because of invalid arguments
             */
            case 11:
                return "INVALID ARGUMENTS";

            /*
             * Failed because of an invalid state
             */
            case 12:
                return "INVALID STATE";

            /*
             * Failed because there is no memory
             */
            case 13:
                return "NO MEMORY";

            /*
             * Failed because the sms format is not valid
             */
            case 14:
                return "INVALID SMS FORMAT";

            /*
             * Failed because of a system error
             */
            case 15:
                return "SYSTEM ERROR";

            /*
             * Failed because of a modem error
             */
            case 16:
                return "MODEM ERROR";

            /*
             * Failed because of a network error
             */
            case 17:
                return "NETWORK ERROR";

            /*
             * Failed because of an encoding error
             */
            case 18:
                return "ENCODING ERROR";

            /*
             * Failed because of an invalid smsc address
             */
            case 19:
                return "INVALID SMSC ADDRESS";

            /*
             * Failed because the operation is not allowed
             */
            case 20:
                return "OPERATION NOT ALLOWED";

            /*
             * Failed because of an internal error
             */
            case 21:
                return "INTERNAL ERROR";

            /*
             * Failed because there are no resources
             */
            case 22:
                return "NO RESOURCES";

            /*
             * Failed because the operation was cancelled
             */
            case 23:
                return "CANCELLED";

            /*
             * Failed because the request is not supported
             */
            case 24:
                return "REQUEST NOT SUPPORTED";

            /*
             * Failed sending via bluetooth because the bluetooth service is not available
             */
            case 25:
                return "NO BLUETOOTH SERVICE";

            /*
             * Failed sending via bluetooth because the bluetooth device address is invalid
             */
            case 26:
                return "INVALID BLUETOOTH ADDRESS";

            /*
             * Failed sending via bluetooth because bluetooth disconnected
             */
            case 27:
                return "BLUETOOTH DISCONNECTED";

            /*
             * Failed sending because the user denied or canceled the dialog displayed for a premium
             * shortcode sms or rate-limited sms.
             */
            case 28:
                return "UNEXPECTED EVENT STOP SENDING";

            /*
             * Failed sending during an emergency call
             */
            case 29:
                return "SMS BLOCKED DURING EMERGENCY";

            /*
             * Failed to send an sms retry
             */
            case 30:
                return "SMS SEND RETRY FAILED";

            /*
             * Set by BroadcastReceiver to indicate a remote exception while handling a message.
             */
            case 31:
                return "REMOTE EXCEPTION";

            /*
             * Set by BroadcastReceiver to indicate there's no default sms app.
             */
            case 32:
                return "NO DEFAULT SMS APP";

            // Radio Error results

            /*
             * The radio did not start or is resetting.
             */
            case 100:
                return "RIL RADIO NOT AVAILABLE";

            /*
             * The radio failed to send the sms and needs to retry.
             */
            case 101:
                return "RIL SMS SEND FAIL RETRY";

            /*
             * The sms request was rejected by the network.
             */
            case 102:
                return "RIL NETWORK REJECT";

            /*
             * The radio returned an unexpected request for the current state.
             */
            case 103:
                return "RIL INVALID STATE";

            /*
             * The radio received invalid arguments in the request.
             */
            case 104:
                return "RIL INVALID ARGUMENTS";

            /*
             * The radio didn't have sufficient memory to process the request.
             */
            case 105:
                return "RIL NO MEMORY";

            /*
             * The radio denied the operation due to overly-frequent requests.
             */
            case 106:
                return "RIL REQUEST RATE LIMITED";

            /*
             * The radio returned an error indicating invalid sms format.
             */
            case 107:
                return "RIL INVALID SMS FORMAT";

            /*
             * The radio encountered a platform or system error.
             */
            case 108:
                return "RIL SYSTEM ERR";

            /*
             * The SMS message was not encoded properly.
             */
            case 109:
                return "RIL ENCODING ERR";

            /*
             * The specified SMSC address was invalid.
             */
            case 110:
                return "RIL INVALID SMSC ADDRESS";

            /*
             * The vendor RIL received an unexpected or incorrect response.
             */
            case 111:
                return "RIL MODEM ERR";

            /*
             * The radio received an error from the network.
             */
            case 112:
                return "RIL NETWORK ERR";

            /*
             * The modem encountered an unexpected error scenario while handling the request.
             */
            case 113:
                return "RIL INTERNAL ERR";

            /*
             * The request was not supported by the radio.
             */
            case 114:
                return "RIL REQUEST NOT SUPPORTED";

            /*
             * The radio cannot process the request in the current modem state.
             */
            case 115:
                return "RIL INVALID MODEM STATE";

            /*
             * The network is not ready to perform the request.
             */
            case 116:
                return "RIL NETWORK NOT READY";

            /*
             * The radio reports the request is not allowed.
             */
            case 117:
                return "RIL OPERATION NOT ALLOWED";

            /*
             * There are insufficient resources to process the request.
             */
            case 118:
                return "RIL NO RESOURCES";

            /*
             * The request has been cancelled.
             */
            case 119:
                return "RIL CANCELLED";

            /*
             * The radio failed to set the location where the CDMA subscription
             * can be retrieved because the SIM or RUIM is absent.
             */
            case 120:
                return "RIL SIM ABSENT";

            // SMS receiving results sent as a "result" extra in Intents.SMS_REJECTED_ACTION

            /*
             * SMS receive dispatch failure.
             */
            case 500:
                return "RECEIVE DISPATCH FAILURE";

            /*
             * SMS receive injected null PDU.
             */
            case 501:
                return "RECEIVE INJECTED NULL PDU";

            /*
             * SMS receive encountered runtime exception.
             */
            case 502:
                return "RECEIVE RUNTIME EXCEPTION";

            /*
             * SMS received null message from the radio interface layer.
             */
            case 503:
                return "RECEIVE NULL MESSAGE FROM RIL";

            /*
             * SMS short code received while the phone is in encrypted state.
             */
            case 504:
                return "RECEIVE WHILE ENCRYPTED";

            /*
             * SMS receive encountered an SQL exception.
             */
            case 505:
                return "RECEIVE SQL EXCEPTION";

            /*
             * SMS receive an exception parsing a uri.
             */
            case 506:
                return "RECEIVE URI EXCEPTION";

            /* Unknown cause */
            default:
                return "ERROR UNKNOWN FAILURE (" + code + ")";
        }

    }
}
