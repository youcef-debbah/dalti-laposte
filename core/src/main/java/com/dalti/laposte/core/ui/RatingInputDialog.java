package com.dalti.laposte.core.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.LongPreference;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.StringSetting;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.common.InputListener;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

public class RatingInputDialog<M> extends InputDialog<Float, M> {

    public static final InputListener<Float, Activity> DEFAULT_LISTENER = new DefaultListener();

    protected RatingBar ratingBar;
    protected String inputStateKey;

    protected RatingInputDialog(M context, View view, MaterialAlertDialogBuilder dialogBuilder,
                                RatingBar ratingBar,
                                InputListener<Float, M> inputListener,
                                String inputStateKey,
                                String shownStateKey,
                                String inputListenerKey) {
        super(context, view, dialogBuilder, inputListener, shownStateKey, inputListenerKey);
        this.ratingBar = Objects.requireNonNull(ratingBar);
        this.inputStateKey = Objects.requireNonNull(inputStateKey);
    }

    @Override
    public Float getValue() {
        RatingBar ratingBar = this.ratingBar;
        return ratingBar != null ? ratingBar.getRating() : null;
    }

    @Override
    public void setValue(Float value) {
        RatingBar ratingBar = this.ratingBar;
        if (value != null && ratingBar != null)
            ratingBar.setRating(GlobalUtil.boundFloat(value, 0, ratingBar.getMax(), 1));
    }

    @Override
    public void saveState(@Nullable Bundle outState) {
        super.saveState(outState);
        RatingBar ratingBar = this.ratingBar;
        if (outState != null && ratingBar != null)
            outState.putFloat(inputStateKey, ratingBar.getRating());
    }

    @Override
    public void show() {
        super.show();
        AppConfig.getInstance().put(LongSetting.CLEARED_ALARMS_COUNT, LongSetting.CLEARED_ALARMS_COUNT.getDefaultLong());
    }

    @Override
    public void dismiss() {
        super.dismiss();
        this.ratingBar = null;
    }

    public static class Builder<VM> extends InputDialog.Builder<Float, VM, RatingInputDialog<VM>> {

        protected int max = 5;
        protected float defaultValue = 0;
        protected String inputStateKey;

        public Builder(@NonNull Context context, @NonNull String name) {
            super(context, name);
            this.inputStateKey = getInstanceNamespace(name) + ".rating";

            final Double rating = StringUtil.parseDouble(AppConfig.getInstance().get(StringSetting.USER_RATING));
            if (rating != null)
                this.defaultValue = rating.floatValue();

            setTitle(R.string.rate_your_exp);
            setPreText(R.string.how_would_you_rate_us);
        }

        public Builder<VM> setMax(LongPreference maxPreference) {
            return setMax(appConfig.getAsInt(maxPreference));
        }

        public Builder<VM> setMax(int max) {
            this.max = max;
            return this;
        }

        public Builder<VM> setDefaultValue(LongPreference defaultValuePreference) {
            return setDefaultValue(appConfig.getAsInt(defaultValuePreference));
        }

        public Builder<VM> setDefaultValue(float defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        public DialogSupplier<RatingInputDialog<VM>> loadState(@NonNull VM listenerContext,
                                                               @NonNull View view,
                                                               @Nullable Bundle savedState) {
            if (savedState != null)
                defaultValue = savedState.getFloat(inputStateKey, defaultValue);
            super.loadState(listenerContext, view, savedState);
            return this;
        }

        @Override
        @NonNull
        public RatingInputDialog<VM> build() {
            int padding = context.getResources().getDimensionPixelOffset(R.dimen.small);
            ViewGroup dialogLayout = ContextUtils.newVerticalLayout(context);

            if (preText != null) {
                TextView textView = new MaterialTextView(context);
                textView.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Subtitle2);
                textView.setPadding(padding, padding, padding, padding);
                textView.setGravity(Gravity.CENTER);
                textView.setText(preText);
                dialogLayout.addView(textView);
            }

            View dialogContent = LayoutInflater.from(context).inflate(R.layout.dialog_rating_input, dialogLayout, true);
            RatingBar ratingBar = dialogContent.findViewById(R.id.rating_input);
            ratingBar.setMax(max);
            ratingBar.setRating(defaultValue);

            if (postText != null) {
                TextView textView = new MaterialTextView(context);
                textView.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Body1);
                textView.setPadding(padding, padding, padding, padding);
                textView.setGravity(Gravity.CENTER);
                textView.setText(postText);
                dialogLayout.addView(textView);
            }

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                    .setCustomTitle(newTitleView(context, title))
                    .setView(dialogLayout);

            return new RatingInputDialog<>(listenerContext, view, builder, ratingBar, inputListener,
                    inputStateKey, shownStateKey, inputListenerKey);
        }
    }

    public static class DefaultListener implements InputListener<Float, Activity> {
        @Override
        public void onInput(Activity activity, View view, Float input) {
            if (input != null) {
                final String rating = String.valueOf(input);
                AppConfig.getInstance().put(StringSetting.USER_RATING, rating);
                if (input >= 4.5f) {
                    QueueUtils.startPlayStore(activity);
                } else {
                    ContextUtils.sendEmail(activity, activity.getString(R.string.send_your_rating),
                            AppConfig.getInstance().getRemoteString(StringSetting.EMAIL),
                            activity.getString(R.string.stars_rating, rating), "");
                }
            }
        }
    }
}
