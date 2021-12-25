package com.dalti.laposte.core.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.NestedScrollView;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.LongPreference;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import dz.jsoftware95.silverbox.android.common.InputListener;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

public class IconInputDialog<C> extends InputDialog<Integer, C> {

    protected final AtomicInteger selectedIcon;
    protected final String selectedIconStateKey;
    protected final List<AppCompatImageView> icons;
    protected NestedScrollView scrollView;

    protected IconInputDialog(C context, View view, MaterialAlertDialogBuilder dialogBuilder, AtomicInteger selectedIcon,
                              List<AppCompatImageView> icons, NestedScrollView scrollView,
                              InputListener<Integer, C> inputListener,
                              String selectedIconStateKey,
                              String shownStateKey,
                              String inputListenerKey) {
        super(context, view, dialogBuilder, inputListener, shownStateKey, inputListenerKey);
        this.selectedIcon = selectedIcon;
        this.selectedIconStateKey = Objects.requireNonNull(selectedIconStateKey);
        this.icons = Objects.requireNonNull(icons);
        this.scrollView = scrollView;
    }

    @Override
    public Integer getValue() {
        return selectedIcon.get();
    }

    @Override
    public void setValue(Integer newIconIndex) {
        int newIndex = NoteState.normalizeIconIndex(newIconIndex);
        if (newIndex != selectedIcon.get()) {
            View.OnClickListener updater = Builder.newIconClickListener(selectedIcon, newIndex);
            updater.onClick(icons.get(newIndex));
        }
    }

    @Override
    public void saveState(@Nullable Bundle outState) {
        super.saveState(outState);
        if (outState != null)
            outState.putInt(selectedIconStateKey, selectedIcon.get());
    }

    @Override
    public void dismiss() {
        super.dismiss();
        icons.clear();
    }

    @Override
    public void show() {
        super.show();
        scrollToSelection();
    }

    private void scrollToSelection() {
        int selection = selectedIcon.get();
        if (scrollView != null && selection < icons.size() && selection > -1) {
            AppCompatImageView selectedIcon = icons.get(selection);
            scrollView.post(() -> scrollView.scrollTo(0, selectedIcon.getTop()));
        }
    }

    public static class Builder<C> extends InputDialog.Builder<Integer, C, IconInputDialog<C>> {

        protected int defaultValue = 0;
        protected String selectedIconStateKey;

        public Builder(@NonNull Context context, @NonNull String name) {
            super(context, name);
            this.selectedIconStateKey = getInstanceNamespace(name) + ".selectedIcon";
        }

        public Builder<C> setDefaultValue(LongPreference defaultValuePreference) {
            return setDefaultValue(appConfig.getAsInt(defaultValuePreference));
        }

        public Builder<C> setDefaultValue(int defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        public DialogSupplier<IconInputDialog<C>> loadState(@NonNull C listenerContext,
                                                            @NonNull View view,
                                                            @Nullable Bundle savedState) {
            if (savedState != null)
                defaultValue = savedState.getInt(selectedIconStateKey, defaultValue);
            super.loadState(listenerContext, view, savedState);
            return this;
        }

        @Override
        @NonNull
        public IconInputDialog<C> build() {
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

            LayoutInflater inflater = LayoutInflater.from(context);
            inflater.inflate(R.layout.component_flex_list, dialogLayout, true);

            AtomicInteger selection = new AtomicInteger();
            FlexboxLayout iconsList = dialogLayout.findViewById(R.id.flex_box);
            List<AppCompatImageView> icons = addIcons(iconsList, selection);
            NestedScrollView scrollView = dialogLayout.findViewById(R.id.scroll_view);

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


            return new IconInputDialog<>(listenerContext, view, builder, selection, icons, scrollView,
                    inputListener, selectedIconStateKey, shownStateKey, inputListenerKey);
        }

        private List<AppCompatImageView> addIcons(FlexboxLayout flexboxLayout, AtomicInteger selection) {
            ArrayList<AppCompatImageView> icons = new ArrayList<>(NoteState.ICONS.length);
            if (flexboxLayout != null) {
                Objects.requireNonNull(selection);
                int iconSize = context.getResources().getDimensionPixelSize(R.dimen.small_thumbnails);
                int iconColorRes = R.color.primary_color_light_selector;
                int spacing = context.getResources().getDimensionPixelSize(R.dimen.tiny);

                for (int i = 0; i < NoteState.ICONS.length; i++) {
                    AppCompatImageView imageView = new AppCompatImageView(context);
                    FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(iconSize, iconSize);
                    params.setMargins(spacing, spacing, spacing, spacing);
                    imageView.setPadding(spacing, spacing, spacing, spacing);
                    imageView.setLayoutParams(params);
                    imageView.setOnClickListener(newIconClickListener(selection, i));
                    imageView.setFocusable(true);

                    VectorDrawableUtil.setDrawable(imageView, NoteState.ICONS[i], iconColorRes);
                    imageView.setImageResource(NoteState.ICONS[i]);
                    flexboxLayout.addView(imageView);
                    icons.add(imageView);
                }

                defaultValue = defaultValue < NoteState.ICONS.length && defaultValue > -1 ? defaultValue : NoteState.DEFAULT_ICON_INDEX;
                newIconClickListener(selection, defaultValue).onClick(flexboxLayout.getChildAt(defaultValue));
            }

            icons.trimToSize();
            return icons;
        }

        static View.OnClickListener newIconClickListener(AtomicInteger selection, int value) {
            return v -> {
                ViewParent parent = v.getParent();
                if (parent instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) parent;
                    int childCount = viewGroup.getChildCount();
                    for (int i = 0; i < childCount; i++)
                        viewGroup.getChildAt(i).setBackground(null);
                }
                v.setBackgroundColor(ContextUtils.getThemeColor(v.getContext(), R.attr.colorAlternativeSurface, R.color.gray_210));
                selection.set(value);
            };
        }
    }
}
