package com.dalti.laposte.core.ui;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.StringSetting;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;

public abstract class AbstractDocActivity extends AbstractQueueActivity {
    private DialogSupplier<RatingInputDialog<Activity>> ratingDialog;

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        DialogSupplier.saveDialog(ratingDialog, outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        ratingDialog = new RatingInputDialog.Builder<Activity>(this, getNamespace() + "_rate_us")
                .loadState(this, Objects.requireNonNull(getBinding()).getRoot(), savedState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DialogSupplier.dismissDialog(ratingDialog);
    }

    @AnyThread
    public void considerRatingDialog() {
        if (StringUtil.isNullOrEmpty(AppConfig.getInstance().get(StringSetting.USER_RATING)))
            newShowRatingDialogJob(this).execute();
    }

    @NotNull
    public static UnJob<AbstractDocActivity> newShowRatingDialogJob(AbstractDocActivity context) {
        return new UnJob<AbstractDocActivity>(context) {
            @Override
            protected void doFromMain(@NonNull AbstractDocActivity context) {
                final AppConfig appConfig = AppConfig.getInstance();
                final long clearedAlarmsCount = appConfig.getLong(LongSetting.CLEARED_ALARMS_COUNT) + 1;
                appConfig.put(LongSetting.CLEARED_ALARMS_COUNT, clearedAlarmsCount);
                if (clearedAlarmsCount >= appConfig.getRemoteLong(LongSetting.CLEARED_ALARMS_TO_SHOW_RATING))
                    context.openRatingDialog();
            }
        };
    }

    @MainThread
    public void openRatingDialog() {
        DialogSupplier.showDialog(ratingDialog, RatingInputDialog.DEFAULT_LISTENER);
    }

    public abstract String getNamespace();
}
