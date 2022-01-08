package com.dalti.laposte.admin.ui;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.model.AdminActionReceiver;
import com.dalti.laposte.admin.repositories.AdminDashboardRepository;
import com.dalti.laposte.core.model.BasicActionReceiver;
import com.dalti.laposte.core.repositories.AdminAction;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.BooleanSetting;
import com.dalti.laposte.core.repositories.DashboardRepository;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.repositories.Selection;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.QueueNotifications;
import com.dalti.laposte.core.ui.Request;
import com.dalti.laposte.core.util.Dimension;
import com.dalti.laposte.core.util.QueueConfig;
import com.dalti.laposte.core.util.QueueUtils;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.queue.common.IdentityManager;
import dz.jsoftware95.silverbox.android.common.ViewLifecycleRegistry;
import dz.jsoftware95.silverbox.android.concurrent.SystemWorker;
import dz.jsoftware95.silverbox.android.frontend.ForegroundService;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

@AndroidEntryPoint
@SuppressLint("ClickableViewAccessibility")
public class CompactDashboardService extends ForegroundService {

    @Inject
    DashboardRepository dashboardRepository;
    @Inject
    AdminDashboardRepository repository;

    @Nullable
    private WindowManager windowManager;
    @Nullable
    private View overlayView;
    @Nullable
    private ViewLifecycleRegistry selectionViewLifecycle;
    @Nullable
    private ToucheListener toucheListener;

    private int width;
    private int currentRank;
    private boolean layoutChanging;

    @Override
    protected Intent newIntent() {
        return new Intent(this, CompactDashboardService.class);
    }

    public int getNotificationID() {
        return QueueNotifications.COMPACT_DASHBOARD_ACTIVATED.id();
    }

    @Override
    protected Notification getNotification() {
        return getNotification(this);
    }

    @Override
    protected boolean showCompatNotification() {
        return AppConfig.getInstance().get(BooleanSetting.SHOW_COMPACT_UI_NOTIFICATION);
    }

    private static Notification getNotification(Context context) {
        try {
            String channelID = context.getString(R.string.ongoing_operations_channel_id);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                    .setSmallIcon(R.drawable.ic_logo_admin_app_24)
                    .setColor(context.getResources().getColor(R.color.brand_color))
                    .setContentTitle(context.getString(R.string.compact_dashboard_notification_title))
                    .setContentText(context.getString(R.string.compact_dashboard_notification_content))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setShowWhen(false)
                    .setContentIntent(newHideOverlayIntent(context))
                    .addAction(R.drawable.ic_baseline_open_in_new_24, context.getString(R.string.open), ContextUtils.newOpenActivityIntent(context, AdminDashboardActivity.class));
            return builder.build();
        } catch (RuntimeException e) {
            Teller.warn("could not get compact dashboard notification");
            return null;
        }
    }

    private static PendingIntent newHideOverlayIntent(Context context) {
        Intent intent = new Intent(context, AdminActionReceiver.class);
        intent.putExtra(BasicActionReceiver.ACTION_KEY, Request.HIDE_COMPACT_UI.name());
        return ContextUtils.getBroadcastIntent(context, Request.HIDE_COMPACT_UI.ordinal(), intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        boolean uiInitialized;
        try {
            updateState(setupUI());
        } catch (RuntimeException e) {
            updateState(false);
            throw new RuntimeException(e);
        }
    }

    private void updateState(boolean shown) {
        AppConfig.getInstance().setCompactDashboardShown(shown);
    }

    @SuppressLint("InflateParams")
    private boolean setupUI() {
        setTheme(R.style.Theme_App_Basic);
        View overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_progress, null);
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        this.overlayView = overlayView;
        this.windowManager = windowManager;
        if (overlayView != null && windowManager != null) {

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    getWindowsType(),
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            //Specify the view position
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = (QueueUtils.getSmallestDisplaySize(windowManager) * 2) / 3;
            windowManager.addView(this.overlayView, params);

            setupViews(params, overlayView);

            final ViewGroup layout = overlayView.findViewById(R.id.layout);
            ViewTreeObserver vto = layout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                private Integer currentOrientation = null;

                @Override
                public void onGlobalLayout() {
                    int orientation = layout.getResources().getConfiguration().orientation;
                    if (currentOrientation == null || currentOrientation != orientation) {
                        Dimension displayDim = QueueUtils.getDisplaySize(CompactDashboardService.this.windowManager);
                        if (displayDim != null) {
                            int width = layout.getMeasuredWidth();
                            CompactDashboardService.this.width = displayDim.getWidth() - width;
                            this.currentOrientation = orientation;
                            ToucheListener listener = CompactDashboardService.this.toucheListener;
                            if (listener != null)
                                listener.positionNearWall(CompactDashboardService.this.windowManager, overlayView);
                        }
                    }
                }
            });

            return true;
        }

        return false;
    }

    private ViewLifecycleRegistry newSelectionViewLifecycle() {
        if (selectionViewLifecycle != null)
            selectionViewLifecycle.markViewAsDestroyed();
        return selectionViewLifecycle = new ViewLifecycleRegistry(this);
    }

    private void setupViews(WindowManager.LayoutParams params, View overlayView) {
        Button incTokenButton = overlayView.findViewById(R.id.inc_token_button);
        Button incWaitingButton = overlayView.findViewById(R.id.inc_waiting_button);
        if (incTokenButton != null && incWaitingButton != null) {
            toucheListener = new ToucheListener(this, params);
            incWaitingButton.setOnTouchListener(toucheListener);
            setupRankUpdates(incTokenButton, incWaitingButton);
        } else
            Teller.logUnexpectedNull(incTokenButton, incWaitingButton);
    }

    private void setupRankUpdates(Button incTokenButton, Button incWaitingButton) {
        incTokenButton.setOnLongClickListener(v -> nextRank(incTokenButton, incWaitingButton));
        incTokenButton.setOnClickListener(button -> {
            SystemWorker.MAIN.executeDelayed(() -> button.setEnabled(true), QueueConfig.INC_TOKEN_DELAY);
            button.setEnabled(false);
            doProgressAction(currentRank, StaticAdminAction.INC_CURRENT);
        });
        incWaitingButton.setOnClickListener(button -> {
            if (!layoutChanging)
                doProgressAction(currentRank, StaticAdminAction.INC_WAITING);
        });

        LiveData<Selection> liveSelection = dashboardRepository.getSelection();
        ViewLifecycleRegistry selectionViewLifecycle = newSelectionViewLifecycle();
        liveSelection.observe(selectionViewLifecycle, selection -> {
            if (selection != null) {
                MutableLiveData<Progress> progress = selection.getProgress(currentRank);
                if (progress != null) {
                    progress.observe(selectionViewLifecycle, data -> {
                        incTokenButton.setText(data.getCurrentTokenText());
                        incWaitingButton.setText(data.getWaitingText());
                    });
                    return;
                }
            }
            incTokenButton.setText(R.string.empty_symbol);
            incWaitingButton.setText(R.string.empty_symbol);
        });
    }

    private boolean nextRank(Button token, Button waiting) {
        Selection value = dashboardRepository.getSelection().getValue();
        if (Selection.hasService(value)) {
            int extra = value.getService().getExtra();
            if (extra == 0)
                QueueUtils.showToast(this, R.string.service_has_one_rank);
            else if (currentRank < extra)
                setRank(currentRank + 1, token, waiting);
            else
                setRank(0, token, waiting);
        } else
            QueueUtils.handleServiceNeeded("change_rank");

        return true;
    }

    private void setRank(int newRank, Button incTokenButton, Button incWaitingButton) {
        this.currentRank = newRank;
        setupRankUpdates(incTokenButton, incWaitingButton);
        QueueUtils.showToast(this, R.string.queue_changed, newRank + 1);
    }

    private void doProgressAction(int rank, AdminAction action) {
        Selection selection = dashboardRepository.getSelection().getValue();
        if (Selection.hasService(selection))
            repository.setToken(action, IdentityManager.getProgressID(selection.getService().getId(), rank));
        else
            QueueUtils.handleServiceMissing();
    }

    private int getWindowsType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            //noinspection deprecation
            return WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        updateState(false);
        if (windowManager != null) {
            if (overlayView != null) {
                windowManager.removeView(overlayView);
                overlayView = null;
            }
            windowManager = null;
            toucheListener = null;
        }
    }

    private static final class ToucheListener implements View.OnTouchListener {

        private static final int longPressTimeout = ViewConfiguration.getLongPressTimeout();

        private final CompactDashboardService service;
        private final WindowManager.LayoutParams params;

        private final int deadSpace;

        private long lastClick;
        private boolean canMove;

        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;

        public ToucheListener(CompactDashboardService service, WindowManager.LayoutParams params) {
            this.service = Objects.requireNonNull(service);
            this.params = params;
            this.deadSpace = service.getResources().getDimensionPixelSize(R.dimen.dead_space);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            WindowManager windowManager = service.windowManager;
            View overlayView = service.overlayView;
            if (windowManager != null && overlayView != null)
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastClick = System.currentTimeMillis();
                        service.layoutChanging = false;
                        canMove = true;
                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        canMove = false;
                        if (service.layoutChanging)
                            positionNearWall(windowManager, overlayView);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (canMove) {
                            int xDiff = Math.round(event.getRawX() - initialTouchX);
                            int yDiff = Math.round(event.getRawY() - initialTouchY);
                            if (xDiff > deadSpace || yDiff > deadSpace || isLongClick()) {
                                service.layoutChanging = true;

                                //Calculate the X and Y coordinates of the view.
                                params.x = initialX + xDiff;
                                params.y = initialY + yDiff;

                                //Update the layout with new X & Y coordinates
                                windowManager.updateViewLayout(overlayView, params);
                            }
                        }
                        break;
                }
            return false;
        }

        /*
         * Logic to auto-position the widget based on where it is positioned currently w.r.t middle of the screen.
         */
        private void positionNearWall(WindowManager windowManager, View overlayView) {
            if (windowManager != null && overlayView != null) {
                int width = service.width;
                int middle = width / 2;
                float nearestXWall = params.x >= middle ? width : 0;
                params.x = (int) nearestXWall;
                windowManager.updateViewLayout(overlayView, params);
            }
        }

        private boolean isLongClick() {
            return System.currentTimeMillis() - lastClick > longPressTimeout;
        }
    }
}
