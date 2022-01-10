package com.dalti.laposte.core.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Vibrator;
import android.telephony.SubscriptionManager;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ArrayRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.annotation.StringRes;
import androidx.annotation.WorkerThread;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.BooleanSetting;
import com.dalti.laposte.core.repositories.ContextInitLogger;
import com.dalti.laposte.core.repositories.Event;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.StateDAO;
import com.dalti.laposte.core.repositories.StateRepository;
import com.dalti.laposte.core.repositories.StringSetting;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.appcheck.FirebaseAppCheck;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import dz.jsoftware95.queue.common.Function;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.api.Situation;
import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.BasicJob;
import dz.jsoftware95.silverbox.android.concurrent.SystemWorker;
import dz.jsoftware95.silverbox.android.middleware.BasicActivity;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;
import okhttp3.HttpUrl;
import retrofit2.Call;

@SuppressWarnings("unused")
public class QueueUtils {

    public static final int SMALLEST_SCREEN_SIZE = 240;
    public static final int DEFAULT_BACKGROUND_SIZE = SMALLEST_SCREEN_SIZE * 2;

    public static final String JAVA_NET_SOCKET_EXCEPTION = "java.net.SocketException";
    public static final String JAVA_NET_SOCKET_TIMEOUT_EXCEPTION = "java.net.SocketTimeoutException";
    public static final String JAVA_NET_UNKNOWN_HOST_EXCEPTION = "java.net.UnknownHostException";
    public static final String[] DNS_EXCEPTIONS = new String[]{JAVA_NET_UNKNOWN_HOST_EXCEPTION};

    private volatile static List<String> languages;

    @MainThread
    public static void showToast(Context context, @StringRes int msg, Object... args) {
        Assert.isMainThread();
        if (context != null)
            showToast(context, context.getString(msg, args));
    }

    @MainThread
    public static void showToast(Context context, String text) {
        Assert.isMainThread();
        if (context != null)
            Toast.makeText(context, text, getToastDuration(text)).show();
    }

    public static void postToast(String text) {
        new BasicJob() {
            @Override
            protected void doFromMain() {
                showToast(AbstractQueueApplication.getInstance(), text);
            }
        }.execute();
    }

    public static void toast(@StringRes int msg, Object... args) {
        toastHelper(msg, args);
    }

    public static void toast(@StringRes int msg, boolean feedbackEnabled) {
        if (feedbackEnabled)
            toastHelper(msg);
    }

    public static void toastHelper(@StringRes int msg, Object... args) {
        if (SystemWorker.MAIN.isCurrentThread())
            showToast(msg, args);
        else
            new BasicJob() {
                @Override
                protected void doFromMain() {
                    showToast(msg, args);
                }
            }.execute();
    }

    private static void showToast(int msg, Object... args) {
        Context context = AbstractQueueApplication.getInstance();
        if (context != null) {
            String msgText = context.getString(msg, args);
            makeToast(context, msgText);
        }
    }

    private static void makeToast(Context context, String msgText) {
        BasicActivity currentActivity = AbstractQueueActivity.CURRENT_STARTED_ACTIVITY;

        String text;
        if (currentActivity != null)
            text = msgText;
        else if (AppConfig.getInstance().get(BooleanSetting.TOAST_FROM_BACKGROUND))
            text = context.getString(R.string.background_toast_template, msgText);
        else
            return;

        Teller.debug("Toast: " + text);
        Toast.makeText(context, text, getToastDuration(msgText)).show();
    }

    private static int getToastDuration(String msg) {
        if (StringUtil.length(msg) > AppConfig.getInstance().getRemoteLong(LongSetting.LONG_TOAST_DURATION_THRESHOLD))
            return Toast.LENGTH_LONG;
        else
            return Toast.LENGTH_SHORT;
    }

    public static void toast(String msg) {
        if (SystemWorker.MAIN.isCurrentThread())
            showToast(msg);
        else
            new BasicJob() {
                @Override
                protected void doFromMain() {
                    showToast(msg);
                }
            }.execute();
    }

    private static void showToast(String msg) {
        Context context = AbstractQueueApplication.getInstance();
        if (context != null)
            makeToast(context, msg);
    }

    public static String getString(@StringRes int resId) {
        return AbstractQueueApplication.requireInstance().getString(resId);
    }

    public static String getString(@StringRes int resId, Object... args) {
        return AbstractQueueApplication.requireInstance().getString(resId, args);
    }

    public static boolean isTestingEnabled(Context context) {
        if (context != null)
            try {
                return context.getResources().getBoolean(R.bool.is_development_stage);
            } catch (RuntimeException e) {
                Teller.warn("could not get project stage", e);
            }

        return false;
    }

    public static boolean isTesting() {
        return isTestingEnabled(AbstractQueueApplication.getInstance());
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    public static void vibrate(long duration) {
        AbstractQueueApplication context = AbstractQueueApplication.getInstance();
        if (context != null) {
            Vibrator vibratorService = ContextCompat.getSystemService(context, Vibrator.class);
            if (vibratorService != null)
                vibratorService.vibrate(duration);
        }
    }

    public static Long getGoogleServicesVersion() {
        return getGoogleServicesVersion(AbstractQueueApplication.getInstance());
    }

    @Nullable
    public static Long getGoogleServicesVersion(Context context) {
        if (context != null)
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0);
                return PackageInfoCompat.getLongVersionCode(packageInfo);
            } catch (RuntimeException | PackageManager.NameNotFoundException e) {
                Teller.warn("could not get google services version", e);
            }

        return null;
    }

    public static int getAndroidSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static void toastActivationOutdated(Long expirationDate) {
        toastActivationOutdated(expirationDate, true);
    }

    public static void toastActivationOutdated(Long expirationDate, boolean feedbackEnabled) {
        if (feedbackEnabled)
            if (expirationDate != null)
                toast(R.string.activation_code_expired_since, TimeUtils.formatAsDateTime(expirationDate));
            else
                toast(R.string.activation_expired);
    }

    public static void toastActivationNeeded(String operation) {
        toastActivationNeeded(true);
        Teller.log(Event.ActivationNeededWarning.NAME, Event.ActivationNeededWarning.Param.SOURCE_OPERATION, operation);
    }

    public static void toastActivationNeeded(boolean feedbackEnabled) {
        if (feedbackEnabled)
            toast(R.string.activation_needed);
    }

    public static void handleServiceMissing() {
        handleServiceNeeded(true);
        Teller.logUnexpectedCondition();
    }

    public static void handleServiceNeeded(String operation) {
        handleServiceNeeded(true);
        Teller.log(Event.ServiceMissingWarning.NAME, Event.ServiceMissingWarning.Param.SOURCE_OPERATION, operation);
    }

    public static void handleServiceNeeded(boolean feedbackEnabled) {
        if (feedbackEnabled)
            toast(R.string.select_service_first);
    }

    public static int getDisplayHeight(@NonNull Activity activity) {
        Dimension dim = getDisplaySize(activity);
        return dim != null ? dim.getHeight() : SMALLEST_SCREEN_SIZE;
    }

    public static int getDisplayWidth(@NonNull Activity activity) {
        Dimension dim = getDisplaySize(activity);
        return dim != null ? dim.getWidth() : SMALLEST_SCREEN_SIZE;
    }

    @Contract(pure = true)
    public static Dimension getDisplaySize(@NonNull Activity activity) {
        return getDisplaySize(activity.getWindowManager());
    }

    @Nullable
    public static Dimension getDisplaySize(WindowManager windowManager) {
        try {
            if (windowManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
                    if (windowMetrics != null) {
                        Rect bounds = windowMetrics.getBounds();
                        return new Dimension(bounds.width(), bounds.height());
                    }
                } else
                    return getDisplayDimensionUsingOldAPI(windowManager);
            }
        } catch (RuntimeException e) {
            Teller.error("could not get display size", e);
        }

        return null;
    }

    @Nullable
    private static Dimension getDisplayDimensionUsingOldAPI(WindowManager windowManager) {
        Display defaultDisplay = windowManager.getDefaultDisplay();
        if (defaultDisplay != null) {
            Point size = new Point();
            defaultDisplay.getSize(size);
            return new Dimension(size.x, size.y);
        }
        return null;
    }

    public static int getSmallestDisplaySize(WindowManager windowManager) {
        Dimension dim = getDisplaySize(windowManager);
        return dim != null ? Math.min(dim.getWidth(), dim.getHeight()) : QueueUtils.SMALLEST_SCREEN_SIZE;
    }

    public static int getSmallestDisplaySize(Activity activity) {
        Dimension dim = getDisplaySize(activity);
        return dim != null ? Math.min(dim.getWidth(), dim.getHeight()) : QueueUtils.SMALLEST_SCREEN_SIZE;
    }

    @SuppressWarnings("ConstantConditions")
    public static String getUrl(Call<?> call) {
        if (call != null) {
            okhttp3.Request request = call.request();
            if (request != null) {
                HttpUrl url = request.url();
                if (url != null)
                    return url.toString();
            }
        }
        return null;
    }

    @Contract("null -> null")
    public static String formatAsDurationOfMinutesAndHours(Long ms) {
        Context context = AbstractQueueApplication.getInstance();
        if (ms == null || context == null)
            return null;

        long hours = TimeUnit.MILLISECONDS.toHours(ms);
        if (hours > 0)
            return context.getString(R.string.s_and_s, getFormattedHours(context, ms),
                    getFormattedMinutes(context, ms - TimeUnit.HOURS.toMillis(hours)));
        else
            return getFormattedMinutes(context, ms);
    }

    private static String getFormattedMinutes(Context context, long ms) {
        final int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(ms);
        return context.getResources().getQuantityString(R.plurals.minutes, minutes, minutes);
    }

    private static String getFormattedHours(Context context, long ms) {
        final int hours = (int) TimeUnit.MILLISECONDS.toHours(ms);
        return context.getResources().getQuantityString(R.plurals.hours, hours, hours);
    }

    @Contract("null -> null")
    public static String formatAsDurationOfMinHour(Long ms) {
        Context context = AbstractQueueApplication.getInstance();
        if (ms == null || context == null)
            return null;

        long hours = TimeUnit.MILLISECONDS.toHours(ms);
        if (hours > 0)
            return context.getString(R.string.duration_in_hour_min, hours,
                    TimeUnit.MILLISECONDS.toMinutes(ms - TimeUnit.HOURS.toMillis(hours)));
        else
            return context.getString(R.string.duration_in_min, TimeUnit.MILLISECONDS.toMinutes(ms));
    }

    @Contract("null -> null")
    public static String formatAsDurationOfSecMin(Long ms) {
        Context context = AbstractQueueApplication.getInstance();
        if (ms == null || context == null)
            return null;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
        if (minutes > 0) {
            long seconds = TimeUnit.MILLISECONDS.toSeconds(ms - TimeUnit.MINUTES.toMillis(minutes));
            if (seconds > 0)
                return context.getString(R.string.duration_in_min_sec, minutes, seconds);
            else
                return context.getString(R.string.duration_in_min, minutes);
        } else
            return context.getString(R.string.duration_in_sec, TimeUnit.MILLISECONDS.toSeconds(ms));
    }

    @Contract("null -> null")
    public static String formatAsDurationOfSecMinCompact(Long ms) {
        Context context = AbstractQueueApplication.getInstance();
        if (ms == null || context == null)
            return null;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
        if (minutes > 0) {
            long seconds = TimeUnit.MILLISECONDS.toSeconds(ms - TimeUnit.MINUTES.toMillis(minutes));
            if (seconds > 0)
                return context.getString(R.string.duration_in_min_sec_compact, minutes, seconds);
            else
                return context.getString(R.string.duration_in_min_compact, minutes);
        } else
            return context.getString(R.string.duration_in_sec_compact, TimeUnit.MILLISECONDS.toSeconds(ms));
    }

    @NonNull
    public static List<String> getLanguages() {
        if (languages == null) {
            languages = ContextUtils.getPreferredLanguages(AbstractQueueApplication.requireInstance());
        }
        return languages;
    }

    @Nullable
    public static String getString(@Nullable String englishText, @Nullable String frenchText, @Nullable String arabicText) {
        return GlobalUtil.getText(englishText, frenchText, arabicText, getLanguages());
    }

    public static String getString(int index, @ArrayRes int optionsArray) {
        AbstractQueueApplication context = AbstractQueueApplication.getInstance();
        return context != null ? StringUtil.getString(index, context.getResources().getStringArray(optionsArray)) : null;
    }

    public static boolean isRightToLeftLayout() {
        return AbstractQueueApplication.requireInstance().getResources().getBoolean(R.bool.is_right_to_left_layout);
    }

    public static boolean isCompactLayout() {
        return AbstractQueueApplication.requireInstance().getResources().getBoolean(R.bool.is_compact_layout);
    }

    public static MaterialTextView newBody2Text(Context context, String text) {
        return newTextView(context, text, R.style.TextAppearance_Jsoftware95_Body2);
    }

    public static MaterialTextView newBody1Text(Context context, String text) {
        return newTextView(context, text, R.style.TextAppearance_Jsoftware95_Body1);
    }

    public static MaterialTextView newSubtitle2Text(Context context, String text) {
        return newTextView(context, text, R.style.TextAppearance_Jsoftware95_Subtitle2);
    }

    public static MaterialTextView newSubtitle1Text(Context context, String text) {
        return newTextView(context, text, R.style.TextAppearance_Jsoftware95_Subtitle1);
    }

    private static MaterialTextView newTextView(Context context, String text, int textAppearance) {
        MaterialTextView textView = new MaterialTextView(context);
        textView.setTextAppearance(context, textAppearance);
        textView.setGravity(Gravity.START);
        textView.setText(text != null ? text : context.getString(R.string.unknown_symbol));
        return textView;
    }

    @ColorInt
    public static int getColor(@ColorRes int colorRes) {
        Assert.that(ContextUtils.isValidID(colorRes));
        AbstractQueueApplication context = AbstractQueueApplication.requireInstance();
        return context.getResources().getColor(colorRes);
    }

    public static ColorStateList getColorStateList(@ColorRes int colorRes) {
        Assert.that(ContextUtils.isValidID(colorRes));
        return AppCompatResources.getColorStateList(AbstractQueueApplication.requireInstance(), colorRes);
    }

    @ColorInt
    public static int getThemeColor(@AttrRes int themeAttribute, @ColorRes int defaultColor) {
        return ContextUtils.getThemeColor(AbstractQueueApplication.requireInstance(), themeAttribute, defaultColor);
    }

    public static void style(SwipeRefreshLayout refreshLayout) {
        if (refreshLayout != null) {
            refreshLayout.setElevation(refreshLayout.getContext().getResources().getDimensionPixelOffset(R.dimen.refresh_layout_elevation));
            refreshLayout.setProgressBackgroundColorSchemeColor(QueueUtils.getThemeColor(R.attr.colorAlternativeSurface, R.color.white));
            refreshLayout.setColorSchemeColors(QueueUtils.getThemeColor(R.attr.colorOnAlternativeSurface, R.color.black));
        }
    }

    @WorkerThread
    public static Situation newSituation(StateDAO stateDAO, BuildConfiguration buildConfiguration) {
        Situation situation = new Situation();
        situation.setVersion(stateDAO.getCurrentSituationVersion());
        situation.setApplicationVersion(buildConfiguration.getSignedVersionCode());
        situation.setApplicationID(AppConfig.getInstance().getApplicationID());
        situation.setAndroidVersion(getAndroidSdkVersion());
        situation.setGoogleVersion(getGoogleServicesVersion());
        situation.setLanguages(getLanguages());
        return situation;
    }

    public static void requestCacheInvalidation() {
        AbstractQueueApplication.enqueue(StateRepository.InvalidateCacheWorker.class, StateRepository.InvalidateCacheWorker.NAME);
        AppConfig.setNoConfigCache(true);
    }

    public static void copyToClipboard(ClipboardManager clipboardManager, String value, @StringRes int label) {
        if (StringUtil.isNullOrEmpty(value))
            QueueUtils.toast(getString(R.string.nothing_to_copy));
        else {
            AbstractQueueApplication context = AbstractQueueApplication.getInstance();
            if (context != null && clipboardManager != null) {
                ClipData clip = ClipData.newPlainText(context.getString(label), value);
                clipboardManager.setPrimaryClip(clip);
                QueueUtils.toast(R.string.copied_to_clipboard, value);
            }
        }
    }

    public static void setEditorAction(TextView view, Function<TextView, Boolean> action, String editorLabel) {
        if (action != null && view != null) {
            view.setOnEditorActionListener((input, actionId, event) -> {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Boolean result = action.apply(input);
                    Teller.log(Event.EditorSubmission.NAME, Event.EditorSubmission.Param.EDITOR_LABEL, editorLabel);
                    return result;
                }
                return false;
            });
        }
    }

    @NotNull
    public static String getSimpleWebPage(Context context, @StringRes int id) {
        return "<html><body>" +
                "<p style='text-align: center;'>" + context.getString(id) + "</p>" +
                "</body></html>";
    }

    public static boolean isSubclassOf(Class<?> instanceType, String targetTypeName) {
        if (GlobalUtil.notEmpty(targetTypeName)) {
            Class<?> type = instanceType;
            while (type != null) {
                if (Objects.equals(type.getName(), targetTypeName))
                    return true;
                type = type.getSuperclass();
            }
        }
        return false;
    }

    public static boolean isCausedBy(Throwable e, String[] exceptions) {
        if (exceptions != null && exceptions.length > 0) {
            Throwable current = e;
            while (current != null) {
                Class<? extends Throwable> throwableType = current.getClass();
                for (String exceptionName : exceptions)
                    if (isSubclassOf(throwableType, exceptionName))
                        return true;
                current = current.getCause();
            }
        }
        return false;
    }

    public static SubscriptionManager getSubscriptionManager(@NonNull Context context) {
        Objects.requireNonNull(context);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                return context.getSystemService(SubscriptionManager.class);
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                //noinspection deprecation
                return SubscriptionManager.from(context);
        } catch (RuntimeException e) {
            Teller.warn("could not get SubscriptionManager", e);
        }
        return null;
    }

    public static void restart() {
        AppWorker.DATABASE.execute(QueueUtils::restartNow);
    }

    public static void restartNow() {
        AbstractQueueApplication context = AbstractQueueApplication.getInstance();
        try {
            if (context != null) {
                PackageManager packageManager = context.getPackageManager();
                if (packageManager != null) {
                    Intent restartIntent = packageManager.getLaunchIntentForPackage(context.getPackageName());
                    if (restartIntent != null) {
                        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        mgr.setExact(AlarmManager.RTC, System.currentTimeMillis() + 1000, ContextUtils.newOpenActivityIntent(context, restartIntent));
                        mgr.setExact(AlarmManager.RTC, System.currentTimeMillis() + 3000, ContextUtils.newOpenActivityIntent(context, restartIntent));
                        Thread.sleep(2000);
                        System.exit(0);
                    } else
                        Teller.logUnexpectedCondition();
                } else
                    Teller.logUnexpectedCondition();
            } else
                Teller.logUnexpectedCondition();
        } catch (RuntimeException e) {
            Teller.warn("could not restart", e);
        } catch (InterruptedException e) {
            Teller.logUnexpectedCondition(e.getMessage());
            System.exit(0);
        }
    }

    @Nullable
    public static String formatAsTimeNewLineDate(Long epoch) {
        if (epoch == null || epoch < 1)
            return null;
        else {
            final Date date = new Date(epoch);
            return getString(R.string.two_lines_layout, TimeUtils.TIME_FORMAT.format(date), TimeUtils.DATE_FORMAT.format(date));
        }
    }

    public static void startPlayStore(Activity activity, String packageName) {
        Objects.requireNonNull(activity);
        Objects.requireNonNull(packageName);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(ContextUtils.getGooglePlayLaunchUri(packageName));
            intent.setPackage("com.android.vending");
            activity.startActivity(intent);
        } catch (RuntimeException e) {
            Teller.warn("could not open app in play store: " + packageName);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(ContextUtils.getGooglePlayLaunchUri(packageName));
            activity.startActivity(intent);
        }
    }

    public static void startPlayStore(Activity activity) {
        startPlayStore(activity, activity.getPackageName());
    }

    public static void openGoogleServicesInPlayStore(Activity activity) {
//        play store urls:
//        stack overflow:
//        https://market.android.com/details?id=
//        dev docs:
//        https://play.google.com/store/apps/details?id=
        startPlayStore(activity, ContextUtils.GOOGLE_SERVICES_PACKAGE_NAME);
    }

    public static void requestPong() {
        AbstractQueueApplication.enqueue(ContextInitLogger.PongWorker.class, ContextInitLogger.PongWorker.NAME);
    }

    public static String formatNumber(Number number) {
        return number != null ? getString(R.string.number_value, number.longValue()) : getString(R.string.unknown_symbol);
    }

    public static void confirmInput(NumberPicker numberPicker) {
        try {
            final TextView textView = getTextInput(numberPicker);
            if (textView != null) {
                final CharSequence inputText = textView.getText();
                if (inputText != null) {
                    final Integer input = StringUtil.parseInteger(inputText.toString());
                    if (input != null)
                        numberPicker.setValue(input);
                }
            }
        } catch (RuntimeException e) {
            Teller.warn("could not confirm number pucker input", e);
        }
    }

    public static TextView getTextInput(View view) {
        try {
            if (view instanceof TextView)
                return (TextView) view;
            else if (view instanceof NumberPicker) {
                NumberPicker numberPicker = (NumberPicker) view;
                final View child = numberPicker.getChildAt(0);
                if (child instanceof TextView)
                    return (TextView) child;
            }
            return null;
        } catch (RuntimeException e) {
            Teller.warn("could not extract picker input", e);
            return null;
        }
    }

    public static Integer getConfirmedInput(NumberPicker numberPicker) {
        if (numberPicker == null)
            return null;
        else {
            confirmInput(numberPicker);
            return numberPicker.getValue();
        }
    }

    public static void sendFeedback(@NonNull Activity activity) {
        final AppConfig appConfig = AppConfig.getInstance();
        ContextUtils.sendEmail(activity,
                activity.getString(R.string.send_us_email),
                appConfig.getRemoteString(StringSetting.EMAIL),
                getString(R.string.feedback_email_subject),
                activity.getString(R.string.feedback_email_text, appConfig.getInAppMessageID()));
    }

    public static String getAppCheckToken() throws InterruptedException {
        final AppConfig appConfig = AppConfig.getInstance();
        if (!appConfig.get(BooleanSetting.ENABLE_APP_CHECK))
            return null;
        else {
            final AtomicReference<String> token = new AtomicReference<>();
            final AtomicBoolean successful = new AtomicBoolean();
            final CountDownLatch latch = new CountDownLatch(1);
            long t0 = System.nanoTime();
            FirebaseAppCheck.getInstance()
                    .getAppCheckToken(appConfig.get(BooleanSetting.FORCE_APP_CHECK_REFRESH))
                    .addOnCompleteListener(result -> {
                        if (result.isSuccessful()) {
                            successful.set(true);
                            final String appCheckToken = result.getResult().getToken();
                            token.set(appCheckToken);
                        } else
                            successful.set(false);
                        latch.countDown();
                    });
            //noinspection ResultOfMethodCallIgnored
            latch.await(AppConfig.getInstance().getRemoteLong(LongSetting.APP_CHECK_TIMEOUT_IN_MILLIS), TimeUnit.MILLISECONDS);
            AppConfig.getInstance().put(StringSetting.LAST_ACTIVATION_APP_CHECK, successful.get() ? "successful" : "failed");
            Teller.logActivationCheckCompleted(t0, successful.get());
            return token.get();
        }
    }

    @NonNull
    public static BasicJob newShowKeyboardJob(@NonNull View view) {
        Objects.requireNonNull(view);
        return new BasicJob() {
            @Override
            protected void doFromMain() {
                final AbstractQueueApplication context = AbstractQueueApplication.getInstance();
                if (context != null) {
                    InputMethodManager info = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (info != null)
                        info.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                    else
                        Teller.logUnexpectedCondition();
                } else
                    Teller.logUnexpectedCondition();
            }
        };
    }

    public static void showKeyboardOnFocus(View view) {
        TextView input = getTextInput(view);
        if (input != null) {
            input.setSelectAllOnFocus(true);
            input.setOnFocusChangeListener((v, hasFocus) -> QueueUtils.newShowKeyboardJob(input).execute());
        }
    }
}
