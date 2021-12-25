package com.dalti.laposte.core.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.LongPreference;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.common.InputListener;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

import static android.view.ViewGroup.LayoutParams;

public class IntegerComboInputDialog<M> extends InputDialog<Integer, M> {

    protected Map<Integer, NumberPicker> numberPickers;
    protected String valueStateKey;

    protected IntegerComboInputDialog(M viewModel, View view, MaterialAlertDialogBuilder dialogBuilder,
                                      Map<Integer, NumberPicker> numberPickers,
                                      InputListener<Integer, M> inputListener,
                                      String valueStateKey,
                                      String shownStateKey,
                                      String inputListenerKey) {
        super(viewModel, view, dialogBuilder, inputListener, shownStateKey, inputListenerKey);
        this.numberPickers = Objects.requireNonNull(numberPickers);
        this.valueStateKey = Objects.requireNonNull(valueStateKey);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (numberPickers != null)
            for (NumberPicker numberPicker : numberPickers.values())
                QueueUtils.confirmInput(numberPicker);
        super.onClick(dialog, which);
    }

    @Override
    public Integer getValue() {
        int result = 0;

        for (Map.Entry<Integer, NumberPicker> entry : numberPickers.entrySet()) {
            Integer inputId = entry.getKey();
            NumberPicker input = entry.getValue();
            if (inputId != null && input != null)
                result = addValue(result, input.getValue(), inputId);
        }

        return result;
    }

    protected int addValue(int currentValue, int newValue, int inputId) {
        return currentValue + newValue * inputId;
    }

    protected int subtractValue(int currentValue, int newValue, int inputId) {
        return currentValue - newValue * inputId;
    }

    @Override
    public void setValue(Integer value) {
        int currentValue = value;
        for (Map.Entry<Integer, NumberPicker> entry : numberPickers.entrySet()) {
            Integer inputId = entry.getKey();
            NumberPicker input = entry.getValue();
            if (input != null && inputId != null) {
                int newInputValue = GlobalUtil.bound(filterValue(currentValue, inputId), input.getMinValue(), input.getMaxValue());
                input.setValue(newInputValue);
                currentValue = subtractValue(currentValue, newInputValue, inputId);
            }
        }
    }

    protected int filterValue(int value, int inputId) {
        return value / inputId;
    }

    @Override
    public void saveState(@Nullable Bundle outState) {
        super.saveState(outState);
        Integer value = getValue();
        if (outState != null && value != null)
            outState.putInt(valueStateKey, value);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        this.numberPickers.clear();
    }

    public static class Builder<VM> extends InputDialog.Builder<Integer, VM, IntegerComboInputDialog<VM>> {

        protected static final int SECONDS_INPUT = 1;
        protected static final int MINUTES_INPUT = SECONDS_INPUT * 60;
        protected static final int HOURS_INPUT = MINUTES_INPUT * 60;

        protected int defaultValue = 0;
        protected String valueStateKey;

        protected List<InputBuilder> inputs = new LinkedList<>();
        protected Comparator<? super Integer> inputComparator = StringUtil::reversedCompare;

        public Builder(@NonNull Context context, @NonNull String name) {
            super(context, name);
            this.valueStateKey = getInstanceNamespace(name) + ".value";
        }

        public InputBuilder newInputBuilder(int id) {
            return new InputBuilder(context, appConfig, id);
        }

        public Builder<VM> add(InputBuilder inputBuilder) {
            inputs.add(Objects.requireNonNull(inputBuilder));
            return this;
        }

        public Builder<VM> addSecondsInput() {
            InputBuilder inputBuilder = newInputBuilder(SECONDS_INPUT)
                    .setMin(0)
                    .setMax(59)
                    .setEndLabel(QueueUtils.isCompactLayout() ? R.string.sec : R.string.seconds);
            add(inputBuilder);
            return this;
        }

        public Builder<VM> addMinutesInput() {
            InputBuilder inputBuilder = newInputBuilder(MINUTES_INPUT)
                    .setMin(0)
                    .setMax(59)
                    .setEndLabel(QueueUtils.isCompactLayout() ? R.string.min : R.string.minutes);
            add(inputBuilder);
            return this;
        }

        public Builder<VM> addHoursInput() {
            InputBuilder inputBuilder = newInputBuilder(HOURS_INPUT)
                    .setMin(0)
                    .setMax(23)
                    .setEndLabel(R.string.hours);
            add(inputBuilder);
            return this;
        }

        public Builder<VM> setInputComparator(Comparator<? super Integer> inputComparator) {
            this.inputComparator = inputComparator;
            return this;
        }

        public Builder<VM> setDefaultValue(LongPreference defaultValuePreference) {
            return setDefaultValue(appConfig.getAsInt(defaultValuePreference));
        }

        public Builder<VM> setDefaultValue(int defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        public DialogSupplier<IntegerComboInputDialog<VM>> loadState(@NonNull VM listenerContext,
                                                                     @NonNull View view,
                                                                     @Nullable Bundle savedState) {
            if (savedState != null)
                defaultValue = savedState.getInt(valueStateKey, defaultValue);
            super.loadState(listenerContext, view, savedState);
            return this;
        }

        @Override
        @NonNull
        public IntegerComboInputDialog<VM> build() {
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

            ViewGroup inputLayout = ContextUtils.newHorizontalLayout(context);
            inputLayout.setPadding(padding, padding, padding, padding);
            Map<Integer, NumberPicker> numberPickers = createNumberPickers(inputLayout, padding);
            dialogLayout.addView(inputLayout);

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

            IntegerComboInputDialog<VM> dialog = new IntegerComboInputDialog<>(listenerContext, view, builder, numberPickers,
                    inputListener, valueStateKey, shownStateKey, inputListenerKey);
            dialog.setValue(defaultValue);
            return dialog;
        }

        private Map<Integer, NumberPicker> createNumberPickers(ViewGroup layout, int padding) {
            Map<Integer, NumberPicker> pickers = new TreeMap<>(inputComparator);

            boolean inputAdded = false;
            for (InputBuilder input : inputs) {
                if (inputAdded)
                    ContextUtils.addPaddingAtEnd(layout.getChildAt(layout.getChildCount() - 1), padding);

                NumberPicker numberPicker = new NumberPicker(context);
                addInputView(layout, input, numberPicker, padding);
                pickers.put(input.id, numberPicker);

                inputAdded = true;
            }

            return pickers;
        }

        private void addInputView(ViewGroup layout, InputBuilder input, NumberPicker numberPicker, int padding) {
            if (input.startLabel != null)
                layout.addView(ContextUtils.addPaddingAtEnd(newTextView(input.startLabel), padding));

            numberPicker.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 0));
            numberPicker.setMaxValue(input.max);
            numberPicker.setMinValue(input.min);
            numberPicker.setValue(defaultValue);
            numberPicker.setGravity(Gravity.CENTER);
            layout.addView(numberPicker);

            if (input.endLabel != null)
                layout.addView(ContextUtils.addPaddingAtStart(newTextView(input.endLabel), padding));
        }

        private View newTextView(String label) {
            TextView textView = new MaterialTextView(context);
            textView.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Body2);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1));
            textView.setText(label);
            textView.setGravity(Gravity.CENTER);
            return textView;
        }

        public static class InputBuilder {

            protected Context context;
            protected AppConfig appConfig;
            protected int id;

            protected int max;
            protected int min;

            protected String startLabel;
            protected String endLabel;

            private InputBuilder(Context context, AppConfig appConfig, int id) {
                this.context = Objects.requireNonNull(context);
                this.appConfig = appConfig;
                this.id = id;
                this.max = Integer.MAX_VALUE & id;
                this.min = 0;
            }

            public InputBuilder setMax(LongPreference maxPreference) {
                return setMax(appConfig.getAsInt(maxPreference));
            }

            public InputBuilder setMax(int max) {
                this.max = max;
                return this;
            }

            public InputBuilder setMin(LongPreference minPreference) {
                return setMin(appConfig.getAsInt(minPreference));
            }

            public InputBuilder setMin(int min) {
                this.min = min;
                return this;
            }

            public InputBuilder setStartLabel(@StringRes int label, Object... args) {
                this.startLabel = context.getString(label, args);
                return this;
            }

            public InputBuilder setStartLabel(@StringRes int label) {
                this.startLabel = context.getString(label);
                return this;
            }

            public InputBuilder setStartLabel(String label) {
                this.startLabel = label;
                return this;
            }

            public InputBuilder setEndLabel(@StringRes int label, Object... args) {
                this.endLabel = context.getString(label, args);
                return this;
            }

            public InputBuilder setEndLabel(@StringRes int label) {
                this.endLabel = context.getString(label);
                return this;
            }

            public InputBuilder setEndLabel(String label) {
                this.endLabel = label;
                return this;
            }
        }
    }
}
