package com.dalti.laposte.core.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.LongPreference;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.common.InputListener;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

import static android.view.ViewGroup.LayoutParams;

public class IntegerInputDialog<M> extends InputDialog<Integer, M> {

    protected NumberPicker numberPicker;
    protected String numberPickerStateKey;

    protected IntegerInputDialog(M viewModel, View view, MaterialAlertDialogBuilder dialogBuilder,
                                 NumberPicker numberPicker,
                                 InputListener<Integer, M> inputListener,
                                 String numberPickerStateKey,
                                 String shownStateKey,
                                 String inputListenerKey) {
        super(viewModel, view, dialogBuilder, inputListener, shownStateKey, inputListenerKey);
        this.numberPicker = Objects.requireNonNull(numberPicker);
        this.numberPickerStateKey = Objects.requireNonNull(numberPickerStateKey);
        QueueUtils.showKeyboardOnFocus(numberPicker);
    }

    @Override
    public void show() {
        super.show();
        final TextView input = QueueUtils.getTextInput(numberPicker);
        if (input != null)
            input.requestFocus();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        QueueUtils.confirmInput(numberPicker);
        super.onClick(dialog, which);
    }

    @Override
    public Integer getValue() {
        NumberPicker numberPicker = this.numberPicker;
        return numberPicker != null ? numberPicker.getValue() : null;
    }

    @Override
    public void setValue(Integer value) {
        NumberPicker numberPicker = this.numberPicker;
        if (value != null && numberPicker != null)
            numberPicker.setValue(GlobalUtil.bound(value, numberPicker.getMinValue(), numberPicker.getMaxValue()));
    }

    @Override
    public void saveState(@Nullable Bundle outState) {
        super.saveState(outState);
        NumberPicker numberPicker = this.numberPicker;
        if (outState != null && numberPicker != null)
            outState.putInt(numberPickerStateKey, numberPicker.getValue());
    }

    @Override
    public void dismiss() {
        super.dismiss();
        this.numberPicker = null;
    }

    public void setMax(int max) {
        NumberPicker numberPicker = this.numberPicker;
        if (numberPicker != null)
            numberPicker.setMaxValue(max);
    }

    public void setMin(int min) {
        NumberPicker numberPicker = this.numberPicker;
        if (numberPicker != null)
            numberPicker.setMinValue(min);
    }

    public static class Builder<VM> extends InputDialog.Builder<Integer, VM, IntegerInputDialog<VM>> {

        protected int max = Integer.MAX_VALUE;
        protected int min = Integer.MIN_VALUE;
        protected int defaultValue = 0;
        protected String numberPickerStateKey;
        protected String startLabel;
        protected String endLabel;

        public Builder(@NonNull Context context, @NonNull String name) {
            super(context, name);
            this.numberPickerStateKey = getInstanceNamespace(name) + ".numberPicker";
        }

        public Builder<VM> setMax(LongPreference maxPreference) {
            return setMax(appConfig.getAsInt(maxPreference));
        }

        public Builder<VM> setMax(int max) {
            this.max = max;
            return this;
        }

        public Builder<VM> setMin(LongPreference minPreference) {
            return setMin(appConfig.getAsInt(minPreference));
        }

        public Builder<VM> setMin(int min) {
            this.min = min;
            return this;
        }

        public Builder<VM> setDefaultValue(LongPreference defaultValuePreference) {
            return setDefaultValue(appConfig.getAsInt(defaultValuePreference));
        }

        public Builder<VM> setDefaultValue(int defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<VM> setStartLabel(@StringRes int label, Object... args) {
            this.startLabel = context.getString(label, args);
            return this;
        }

        public Builder<VM> setStartLabel(@StringRes int label) {
            this.startLabel = context.getString(label);
            return this;
        }

        public Builder<VM> setStartLabel(String label) {
            this.startLabel = label;
            return this;
        }

        public Builder<VM> setEndLabel(@StringRes int label, Object... args) {
            this.endLabel = context.getString(label, args);
            return this;
        }

        public Builder<VM> setEndLabel(@StringRes int label) {
            this.endLabel = context.getString(label);
            return this;
        }

        public Builder<VM> setEndLabel(String label) {
            this.endLabel = label;
            return this;
        }

        @Override
        public DialogSupplier<IntegerInputDialog<VM>> loadState(@NonNull VM listenerContext,
                                                                @NonNull View view,
                                                                @Nullable Bundle savedState) {
            if (savedState != null)
                defaultValue = savedState.getInt(numberPickerStateKey, defaultValue);
            super.loadState(listenerContext, view, savedState);
            return this;
        }

        @Override
        @NonNull
        public IntegerInputDialog<VM> build() {
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

            NumberPicker numberPicker = new NumberPicker(context);
            numberPicker.setMaxValue(max);
            numberPicker.setMinValue(min);
            numberPicker.setValue(defaultValue);
            dialogLayout.addView(newInputLayout(numberPicker, padding));

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

            return new IntegerInputDialog<>(listenerContext, view, builder, numberPicker, inputListener,
                    numberPickerStateKey, shownStateKey, inputListenerKey);
        }

        private View newInputLayout(NumberPicker numberPicker, int padding) {
            ViewGroup inputLayout = ContextUtils.newHorizontalLayout(context);
            inputLayout.setPadding(padding, padding, padding, padding);

            if (startLabel != null) {
                TextView textView = new MaterialTextView(context);
                textView.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Body2);
                textView.setLayoutParams(new LinearLayoutCompat.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1));
                textView.setText(startLabel);
                textView.setGravity(Gravity.CENTER);
                inputLayout.addView(textView);
            }

            numberPicker.setLayoutParams(new LinearLayoutCompat.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 0));
            numberPicker.setGravity(Gravity.CENTER);
            inputLayout.addView(numberPicker);

            if (endLabel != null) {
                TextView textView = new MaterialTextView(context);
                textView.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Body2);
                textView.setLayoutParams(new LinearLayoutCompat.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1));
                textView.setText(endLabel);
                textView.setGravity(Gravity.CENTER);
                inputLayout.addView(textView);
            }

            return inputLayout;
        }
    }
}
