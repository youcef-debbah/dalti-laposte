package dz.jsoftware95.silverbox.android.frontend;

import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleService;

import java.lang.ref.WeakReference;

public abstract class ForegroundService extends LifecycleService {

    private static final String TAG = "ForegroundService";

    private final ForegroundBinder binder = new ForegroundBinder(this);

    protected abstract Intent newIntent();

    protected abstract Notification getNotification();

    protected abstract int getNotificationID();

    protected boolean showCompatNotification() {
        return true;
    }

    @Override
    @CallSuper
    public void onCreate() {
        super.onCreate();
        if (showCompatNotification() && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(getNotificationID(), getNotification());
        }
    }

    @Nullable
    @Override
    @CallSuper
    public IBinder onBind(@NonNull Intent intent) {
        super.onBind(intent);
        return binder;
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(getNotificationID());
        }
        super.onDestroy();
    }

    public static class ForegroundBinder extends Binder {
        private final WeakReference<ForegroundService> serviceReference;

        public ForegroundBinder(ForegroundService service) {
            this.serviceReference = new WeakReference<>(service);
        }

        public ForegroundService getService() {
            return serviceReference.get();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static class UpgradeToForegroundConnection implements ServiceConnection {

        private final WeakReference<Activity> activityReference;

        public UpgradeToForegroundConnection(Activity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Activity activity = activityReference.get();
            if (activity != null) {
                if (binder instanceof ForegroundBinder) {
                    ForegroundService service = ((ForegroundBinder) binder).getService();
                    if (service != null) {
                        Intent serviceIntent = service.newIntent();
                        Notification serviceNotification = service.getNotification();
                        int serviceNotificationID = service.getNotificationID();

                        activity.startForegroundService(serviceIntent);
                        service.startForeground(serviceNotificationID, serviceNotification);
                    }
                    activity.unbindService(this);
                }

                activityReference.clear();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onBindingDied(ComponentName name) {

        }

        @Override
        public void onNullBinding(ComponentName name) {

        }
    }
}
