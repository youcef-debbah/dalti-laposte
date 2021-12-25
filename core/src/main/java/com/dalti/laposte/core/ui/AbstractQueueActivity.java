package com.dalti.laposte.core.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dz.jsoftware95.silverbox.android.backend.BackendEvent;
import dz.jsoftware95.silverbox.android.backend.DataEvent;
import dz.jsoftware95.silverbox.android.concurrent.SystemWorker;
import dz.jsoftware95.silverbox.android.middleware.BasicActivity;
import dz.jsoftware95.silverbox.android.observers.UnMainObserver;

public abstract class AbstractQueueActivity extends BasicActivity {

    @Inject
    QueueActivitySupport menuSupport;

    private final BasicHandler basicHandler = new BasicHandler(this);

    public BasicHandler getBasicHandler() {
        return basicHandler;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setupLayout(savedState);
        setupActionBar();
        AppConfig.getInstance().updateLastUserInteraction();
    }

    protected void setupLayout(Bundle savedState) {
    }

    private void setupActionBar() {
        Toolbar toolbar = findViewById(getActionBarID());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            setupActionBar(actionBar);
    }

    protected void setupActionBar(@NotNull ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    protected Integer getActionBarID() {
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return menuSupport.inflate(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (menuSupport.handleItemSelected(this, item))
            return true;
        else
            return super.onOptionsItemSelected(item);
    }

    @NotNull
    public static UnMainObserver<Activity, BackendEvent> newSuccessListener(Activity activity) {
        return new UnMainObserver<Activity, BackendEvent>(activity) {
            @Override
            protected void onUpdate(@NonNull Activity activity, @Nullable BackendEvent event) {
                if (event == DataEvent.SUCCESSFUL_UPDATE)
                    activity.finish();
            }
        };
    }

    @NotNull
    public static OnBackPressedCallback newDoubleBackListener() {
        return new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setEnabled(false);
                SystemWorker.MAIN.executeDelayed(() -> setEnabled(true),
                        AppConfig.getInstance().getRemoteLong(LongSetting.CLOSE_DELAY));
                QueueUtils.toast(R.string.press_back_again);
            }
        };
    }
}
