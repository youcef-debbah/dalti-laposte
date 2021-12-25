package com.dalti.laposte.core.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.StringPreference;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import dz.jsoftware95.silverbox.android.common.InputListener;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

public class TextInputDialog<C> extends InputDialog<String, C> {

    protected TextView textView;
    protected String textViewStateKey;

    protected TextInputDialog(C context, View view, MaterialAlertDialogBuilder dialogBuilder,
                              TextView textView,
                              InputListener<String, C> inputListener,
                              String textViewStateKey,
                              String shownStateKey,
                              String inputListenerKey) {
        super(context, view, dialogBuilder, inputListener, shownStateKey, inputListenerKey);
        this.textView = Objects.requireNonNull(textView);
        this.textViewStateKey = Objects.requireNonNull(textViewStateKey);
        QueueUtils.setEditorAction(textView, input -> {
            onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            return false;
        }, shownStateKey + "_input");
    }

    @Override
    public String getValue() {
        return StringUtil.getString(textView);
    }

    @Override
    public void setValue(String value) {
        TextView textView = this.textView;
        if (textView != null)
            textView.setText(value != null ? value : "");
    }

    @Override
    public void saveState(@Nullable Bundle outState) {
        super.saveState(outState);
        if (outState != null)
            outState.putString(textViewStateKey, getValue());
    }

    @Override
    public void dismiss() {
        super.dismiss();
        this.textView = null;
    }

    public static class Builder<C> extends InputDialog.Builder<String, C, TextInputDialog<C>> {

        protected String defaultValue = "";
        protected String textViewStateKey;
        protected String prefix;
        protected String suffix;
        protected String hint;
        protected Integer counter;
        protected Integer inputType;
        protected Integer imeOptions;
        protected KeyListener keyListener;
        protected Integer layoutDirection;

        public Builder(@NonNull Context context, @NonNull String name) {
            super(context, name);
            this.textViewStateKey = getInstanceNamespace(name) + ".text";
        }

        public Builder<C> setDefaultValue(StringPreference defaultValuePreference) {
            return setDefaultValue(appConfig.get(defaultValuePreference));
        }

        public Builder<C> setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<C> setPrefix(@StringRes int label, Object... args) {
            this.prefix = context.getString(label, args);
            return this;
        }

        public Builder<C> setPrefix(@StringRes int label) {
            this.prefix = context.getString(label);
            return this;
        }

        public Builder<C> setPrefix(String label) {
            this.prefix = label;
            return this;
        }

        public Builder<C> setSuffix(@StringRes int label, Object... args) {
            this.suffix = context.getString(label, args);
            return this;
        }

        public Builder<C> setSuffix(@StringRes int label) {
            this.suffix = context.getString(label);
            return this;
        }

        public Builder<C> setSuffix(String label) {
            this.suffix = label;
            return this;
        }

        public Builder<C> setHint(@StringRes int label, Object... args) {
            this.hint = context.getString(label, args);
            return this;
        }

        public Builder<C> setHint(@StringRes int label) {
            this.hint = context.getString(label);
            return this;
        }

        public Builder<C> setHint(String label) {
            this.hint = label;
            return this;
        }

        public Builder<C> setCounter(Integer counter) {
            this.counter = counter;
            return this;
        }

        public Builder<C> setKeyListener(KeyListener keyListener) {
            this.keyListener = keyListener;
            return this;
        }

        public Builder<C> setInputType(Integer inputType) {
            this.inputType = inputType;
            return this;
        }

        public Builder<C> setImeOptions(Integer imeOptions) {
            this.imeOptions = imeOptions;
            return this;
        }

        public Builder<C> setLayoutDirection(Integer layoutDirection) {
            this.layoutDirection = layoutDirection;
            return this;
        }

        @Override
        public DialogSupplier<TextInputDialog<C>> loadState(@NonNull C listenerContext,
                                                            @NonNull View view,
                                                            @Nullable Bundle savedState) {
            if (savedState != null)
                defaultValue = savedState.getString(textViewStateKey, defaultValue);
            super.loadState(listenerContext, view, savedState);
            return this;
        }

        @Override
        @NonNull
        public TextInputDialog<C> build() {
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

            View dialogContent = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, dialogLayout, true);

            TextView inputText = dialogContent.findViewById(R.id.text_input);
            inputText.setMaxLines(1);
            inputText.setText(defaultValue);
            if (inputType != null)
                inputText.setInputType(inputType);
            if (imeOptions != null)
                inputText.setImeOptions(imeOptions);
            if (keyListener != null)
                inputText.setKeyListener(keyListener);

            TextInputLayout textInputLayout = dialogContent.findViewById(R.id.text_input_layout);
            if (textInputLayout != null) {
                if (hint != null)
                    textInputLayout.setHint(hint);
                if (counter != null) {
                    textInputLayout.setCounterMaxLength(counter);
                    textInputLayout.setCounterEnabled(true);
                }
                if (prefix != null)
                    textInputLayout.setPrefixText(prefix);
                if (suffix != null)
                    textInputLayout.setSuffixText(suffix);
                if (layoutDirection != null)
                    textInputLayout.setLayoutDirection(layoutDirection);
            }

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

            return new TextInputDialog<>(listenerContext, view, builder, inputText, inputListener,
                    textViewStateKey, shownStateKey, inputListenerKey);
        }
    }
}
