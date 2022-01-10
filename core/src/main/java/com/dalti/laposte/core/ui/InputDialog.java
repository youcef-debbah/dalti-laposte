package com.dalti.laposte.core.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.Event;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import dz.jsoftware95.silverbox.android.common.InputListener;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

public abstract class InputDialog<T, C> implements DialogInterface.OnClickListener {

    protected C context;
    protected View view;
    protected AlertDialog dialog;

    protected boolean shown;
    protected InputListener<T, C> inputListener;

    private final String shownStateKey;
    private final String inputListenerKey;

    protected InputDialog(C context, View view, MaterialAlertDialogBuilder dialogBuilder,
                          InputListener<T, C> inputListener, String shownStateKey, String inputListenerKey) {
        this.context = Objects.requireNonNull(context);
        this.view = Objects.requireNonNull(view);
        this.dialog = dialogBuilder
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
        setInputListener(inputListener);
        this.shownStateKey = Objects.requireNonNull(shownStateKey);
        this.inputListenerKey = Objects.requireNonNull(inputListenerKey);
        this.dialog.setOnDismissListener(this::onDismiss);
    }

    @NotNull
    public static MaterialTextView newTitleView(@NonNull Context context, @StringRes int title) {
        return newTitleView(context, context.getString(title));
    }

    @NotNull
    public static MaterialTextView newTitleView(@NonNull Context context, String title) {
        int padding = context.getResources().getDimensionPixelOffset(R.dimen.large);
        MaterialTextView textView = new MaterialTextView(context);
        textView.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Headline6);
        textView.setPadding(padding, padding, padding, padding);
        textView.setTextColor(AppCompatResources.getColorStateList(context, R.color.primary_color_selector));
        textView.setGravity(Gravity.CENTER);
        textView.setText(title);
        textView.setMaxLines(1);
        return textView;
    }

    private void setInputListener(InputListener<T, C> inputListener) {
        this.inputListener = inputListener;
        if (view != null && QueueUtils.isTestingEnabled(view.getContext()))
            ContextUtils.checkSerialization(inputListener);
    }

    public void show() {
        if (dialog != null) {
            Teller.log(Event.ShowDialog.NAME, Event.ShowDialog.Param.DIALOG_NAME, shownStateKey);
            shown = true;
            dialog.show();
        }
    }

    public void show(InputListener<T, C> listener) {
        if (listener != null)
            setInputListener(listener);
        show();
    }

    public void hide() {
        if (dialog != null)
            dialog.dismiss();
    }

    public void dismiss() {
        hide();
        dialog = null;
        view = null;
        context = null;
        setInputListener(null);
    }

    @Nullable
    public abstract T getValue();

    public abstract void setValue(T value);

    @CallSuper
    public void saveState(@Nullable Bundle outState) {
        if (outState != null) {
            outState.putBoolean(shownStateKey, shown);
            outState.putSerializable(inputListenerKey, inputListener);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        C context = this.context;
        View view = this.view;
        InputListener<T, C> inputListener = this.inputListener;
        if (inputListener != null && view != null && context != null)
            inputListener.onInput(context, view, which == DialogInterface.BUTTON_POSITIVE ? getValue() : null);

        hide();
    }

    @CallSuper
    protected void onDismiss(DialogInterface dialogInterface) {
        Teller.log(Event.HideDialog.NAME, Event.HideDialog.Param.DIALOG_NAME, shownStateKey);
        this.shown = false;
    }

    public static abstract class Builder<V, C, D extends InputDialog<V, C>> implements DialogSupplier<D> {
        protected Context context;
        protected C listenerContext;
        protected View view;
        protected String title;
        protected String preText;
        protected String postText;
        protected AppConfig appConfig;
        protected String shownStateKey;
        protected String inputListenerKey;
        protected InputListener<V, C> inputListener;
        protected D cache;

        public Builder(@NonNull Context context, String name) {
            this.context = Objects.requireNonNull(context);
            this.appConfig = AppConfig.getInstance();
            String namespace = getInstanceNamespace(name);
            this.shownStateKey = namespace;
            this.inputListenerKey = namespace + ".inputListener";
        }

        public Builder<V, C, D> setTitle(@StringRes int title) {
            return setTitle(context.getString(title));
        }

        public Builder<V, C, D> setTitle(@StringRes int title, Object... args) {
            return setTitle(context.getString(title, args));
        }

        public Builder<V, C, D> setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder<V, C, D> setPrefix(@StringRes int text) {
            return setPreText(context.getString(text));
        }

        public Builder<V, C, D> setPreText(@StringRes int text, Object... args) {
            return setPreText(context.getString(text, args));
        }

        public Builder<V, C, D> setPreText(String preText) {
            this.preText = preText;
            return this;
        }

        public Builder<V, C, D> setPostText(@StringRes int text) {
            return setPostText(context.getString(text));
        }

        public Builder<V, C, D> setPostText(@StringRes int text, Object... args) {
            return setPostText(context.getString(text, args));
        }

        public Builder<V, C, D> setPostText(String postText) {
            this.postText = postText;
            return this;
        }

        public Builder<V, C, D> setInputListener(InputListener<V, C> inputListener) {
            this.inputListener = inputListener;
            return this;
        }

        @CallSuper // call this super method AFTER child code
        public DialogSupplier<D> loadState(@NonNull C listenerContext,
                                           @NonNull View view,
                                           @Nullable Bundle savedState) {
            this.listenerContext = Objects.requireNonNull(listenerContext);
            this.view = Objects.requireNonNull(view);

            if (savedState != null && savedState.getBoolean(shownStateKey, false)) {
                //noinspection unchecked
                InputListener<V, C> listener = (InputListener<V, C>) savedState.getSerializable(inputListenerKey);
                get().show(listener);
            }

            return this;
        }

        @NotNull
        protected String getInstanceNamespace(@NotNull String name) {
            return Objects.requireNonNull(name);
        }

        @Override
        @NonNull
        public D get() {
            if (cache == null)
                cache = build();
            return cache;
        }

        @Override
        @Nullable
        public D getExisting() {
            return cache;
        }

        public abstract D build();
    }
}
