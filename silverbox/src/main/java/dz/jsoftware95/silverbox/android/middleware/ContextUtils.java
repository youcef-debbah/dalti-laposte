package dz.jsoftware95.silverbox.android.middleware;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import dz.jsoftware95.queue.common.Function;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.common.Cache;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.common.StringUtil;

@SuppressWarnings("UnusedReturnValue")
@MainThread
public class ContextUtils {
    public static final ConcurrentMap<Long, Long> AUTO_INFO_FETCH_CACHE = new ConcurrentHashMap<>(4);
    public static final ConcurrentMap<Long, Long> AUTO_INFO_REFRESH_CACHE = new ConcurrentHashMap<>(4);

    public static final String GOOGLE_SERVICES_PACKAGE_NAME = "com.google.android.gms";
    public static final long MIN_AUTO_FETCH_DELAY = TimeUnit.SECONDS.toMillis(35);

    public static final Function<TextView, Boolean> HANDLE_NO_ERROR_ACTION = input -> input != null && input.getError() != null;

    private static final String TAG = ContextUtils.class.getSimpleName();

    private static final Cache<Integer, Bitmap> WHITE_BACKGROUNDS = new Cache<>();
    private static final Cache<Integer, Bitmap> BLACK_BACKGROUNDS = new Cache<>();

    public static final Integer VIEW_VISIBLE = View.INVISIBLE ^ View.GONE;
    public static final Integer VIEW_INVISIBLE = View.INVISIBLE;
    public static final Integer VIEW_GONE = View.GONE;

    private static final AtomicInteger requestsCounter = new AtomicInteger(0);
    private static final int REQUESTS_OFFSET_OPEN_ACTIVITY = 1024;

    private ContextUtils() {
        throw new IllegalAccessError("you can't instantiate static utility class");
    }

    @AnyThread
    public static boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }

    public static boolean containsKey(Intent intent, String key) {
        Check.nonNull(key);
        if (intent == null)
            return false;
        else {
            Bundle extras = intent.getExtras();
            return extras != null && extras.containsKey(key);
        }
    }

    public static Bitmap getWhiteBackground(int dimension) {
        return WHITE_BACKGROUNDS.computeIfAbsent(dimension, d -> createBitmap(d, d, Color.WHITE));
    }

    public static Bitmap getBlackBackground(int dimension) {
        return BLACK_BACKGROUNDS.computeIfAbsent(dimension, d -> createBitmap(d, d, Color.BLACK));
    }

    @AnyThread
    private static Bitmap createBitmap(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        return bitmap;
    }

    public static void checkSerialization(Serializable serializable) {
        if (serializable != null) {
            Bundle bundle = new Bundle(1);
            bundle.putSerializable("key", serializable);

            Parcel parcel = Parcel.obtain();
            bundle.writeToParcel(parcel, 0);
            parcel.recycle();
        }
    }

    public static View findViewWithNestedTags(View root, String tag1, String tag2) {
        Objects.requireNonNull(tag1);
        Objects.requireNonNull(tag2);
        if (root != null) {
            View nestedView = root.findViewWithTag(tag1);
            if (nestedView != null)
                return nestedView.findViewWithTag(tag2);
        }

        return null;
    }

    public static List<String> getPreferredLanguages(@NotNull Context context) {
        List<String> preferredLanguages = new LinkedList<>();
        try {
            Configuration configuration = context.getResources().getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LocaleList locales = configuration.getLocales();
                if (locales != null) {
                    for (int i = 0; i < locales.size(); i++) {
                        Locale locale = locales.get(i);
                        String userLang = locale.getLanguage();
                        for (String lang : GlobalConf.LANGUAGES)
                            if (userLang.startsWith(lang))
                                preferredLanguages.add(lang);
                    }
                }
            } else {
                String userLang = configuration.locale.getLanguage();
                for (String lang : GlobalConf.LANGUAGES)
                    if (userLang.startsWith(lang))
                        preferredLanguages.add(lang);
            }

        } catch (RuntimeException e) {
            Log.e(TAG, "could not get current local", e);
        }

        if (!preferredLanguages.contains(GlobalConf.EN))
            preferredLanguages.add(GlobalConf.EN);

        if (!preferredLanguages.contains(GlobalConf.FR))
            preferredLanguages.add(GlobalConf.FR);

        if (!preferredLanguages.contains(GlobalConf.AR))
            preferredLanguages.add(GlobalConf.AR);

        return preferredLanguages;
    }

    public static byte[] decodeData(String input) {
        if (input == null || input.isEmpty())
            return null;
        else
            return GlobalUtil.decompressData(Base64.decode(input, Base64.DEFAULT));
    }

    public static ViewGroup newLinearLayout(Context context, boolean isVertical) {
        return isVertical ? newVerticalLayout(context) : newHorizontalLayout(context);
    }

    public static ViewGroup newVerticalLayout(Context context) {
        LinearLayoutCompat layout = new LinearLayoutCompat(Objects.requireNonNull(context));
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayoutCompat.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        return layout;
    }

    public static ViewGroup newHorizontalLayout(Context context) {
        LinearLayoutCompat layout = new LinearLayoutCompat(Objects.requireNonNull(context));
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayoutCompat.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        return layout;
    }

    public static <T extends View> T addPaddingAtEnd(T view, int padding) {
        if (view != null)
            view.setPaddingRelative(view.getPaddingStart(), view.getPaddingTop(), view.getPaddingEnd() + padding, view.getPaddingBottom());
        return view;
    }

    public static <T extends View> T addPaddingAtStart(T view, int padding) {
        if (view != null)
            view.setPaddingRelative(view.getPaddingStart() + padding, view.getPaddingTop(), view.getPaddingEnd(), view.getPaddingBottom());
        return view;
    }

    @ColorInt
    public static int getThemeColor(@NonNull Context context, @AttrRes int attribute, @ColorRes int defaultColor) {
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attribute, outValue, true)
                && outValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && outValue.type <= TypedValue.TYPE_LAST_COLOR_INT)
            return outValue.data;
        else
            return context.getResources().getColor(defaultColor);
    }

    /**
     * Checks whether the integer {@code id} is a valid resource ID, as generated by AAPT.
     * <p>Note that a negative integer is not necessarily an invalid resource ID, and custom
     * validations that compare the {@code id} against {@code 0} are incorrect.</p>
     *
     * @param id The integer to validate.
     * @return {@code true} if the integer is a valid resource ID.
     */
    public static boolean isValidID(Integer id) {
        // With the introduction of packages with IDs > 0x7f, resource IDs can be negative when
        // represented as a signed Java int. Some legacy code assumes -1 is an invalid resource ID,
        // despite the existing documentation.
        return id != null && id != -1 && (id & 0xff000000) != 0 && (id & 0x00ff0000) != 0;
    }

    public static void setVisibility(View view, int visibility) {
        if (visibility == VIEW_INVISIBLE)
            view.setVisibility(View.INVISIBLE);
        else if (visibility == VIEW_GONE)
            view.setVisibility(View.GONE);
        else
            view.setVisibility(View.VISIBLE);
    }

    public static void setSize(View view, int size) {
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.width = size;
                layoutParams.height = size;
            }
        }
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        Objects.requireNonNull(serviceName);
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
                if (serviceName.equals(service.service.getClassName()))
                    return true;
        } catch (RuntimeException e) {
            Log.e(TAG, "could not get service running state for: " + serviceName, e);
        }
        return false;
    }

    public static Uri getUri(Context context, Integer rawRes) {
        return rawRes != null ? Uri.parse("android.resource://" + context.getPackageName() + "/" + rawRes) : null;
    }

    public static PendingIntent newOpenActivityIntent(Context context, Class<? extends Activity> activityClass) {
        Intent intent = new Intent(context, activityClass);
        return newOpenActivityIntent(context, intent);
    }

    public static PendingIntent newOpenActivityIntent(Context context, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        int requestCode = REQUESTS_OFFSET_OPEN_ACTIVITY + requestsCounter.getAndIncrement();
        return stackBuilder.getPendingIntent(requestCode, immutableIntentFlags());
    }

    public static boolean isPermissionGranted(Context context, String permissionName) {
//        return context.getPackageManager().checkPermission(permissionName, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
        return ActivityCompat.checkSelfPermission(context, permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isPermissionsGranted(Context context, String[] permissions) {
        if (permissions != null)
            for (String permission : permissions)
                if (!isPermissionGranted(context, permission))
                    return false;

        return true;
    }

    public static boolean canDrawOverlays(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    public static Intent getSmsIntent(String phone, String text) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            intent.setType("vnd.android-dir/mms-sms");
        if (!StringUtil.isBlank(phone))
            intent.putExtra("address", phone);
        if (!StringUtil.isBlank(text))
            intent.putExtra("sms_body", text);
        return intent;
    }

    public static void setDisplay(NumberPicker durationInput, String format) {
        if (durationInput != null && format != null) {
            int max = durationInput.getMaxValue();
            int min = durationInput.getMinValue();
            int count = max - min + 1;
            if (count > 0) {
                String[] labels = new String[count];
                for (int i = 0; i < count; i++)
                    labels[i] = String.format(format, i + min);
                durationInput.setDisplayedValues(labels);
            }
        }
    }

    public static PendingIntent getBroadcastIntent(Context context, int request, Intent intent) {
        return PendingIntent.getBroadcast(context, request, intent, immutableIntentFlags());
    }

    public static int immutableIntentFlags() {
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags |= PendingIntent.FLAG_IMMUTABLE;
        return flags;
    }

    public static String getExtraString(Context context, String key) {
        if (context instanceof Activity) {
            Intent intent = ((Activity) context).getIntent();
            if (intent != null)
                return intent.getStringExtra(key);
        }
        return null;
    }

    @Deprecated
    public static void openPlayStore(Context context, String packageName) {
        // you can also use BuildConfig.APPLICATION_ID
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        // we only want the play store so no query permission is needed
        @SuppressLint("QueryPermissionsNeeded") final List<ResolveInfo> otherApps = context.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp : otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // task re-parenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            context.startActivity(webIntent);
        }
    }

    public static Uri getGooglePlayLaunchUri(String packageName) {
        return Uri.parse("https://play.google.com/store/apps/details")
                .buildUpon()
                .appendQueryParameter("id", packageName)
//                .appendQueryParameter("launch", "true")
                .build();
    }

    public static void openAppSettings(Activity activity, String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", Objects.requireNonNull(packageName), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public static void sendEmail(@NonNull Activity activity, String chooserTitle,
                                 String email, String subject, String text) {
        final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        activity.startActivity(Intent.createChooser(emailIntent, chooserTitle));
    }

    public static void openWebSite(@NonNull Activity activity, @NotNull String url) {
        Objects.requireNonNull(url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        activity.startActivity(intent);
    }

    public static boolean isPlainModeOn(@NonNull Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    public static boolean isWifiOn(@NonNull Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.WIFI_ON, 0) != 0;
    }

    public static boolean isDataRoamingOn(@NonNull Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.DATA_ROAMING, 0) != 0;
    }

    public static int getZenMode(@NonNull Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "zen_mode", -3);
    }
}
