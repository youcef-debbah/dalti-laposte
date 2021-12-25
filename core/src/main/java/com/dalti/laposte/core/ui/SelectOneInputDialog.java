package com.dalti.laposte.core.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.LongPreference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import dz.jsoftware95.silverbox.android.common.InputListener;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

public class SelectOneInputDialog<M> extends InputDialog<Integer, M> {
    private static final int ID_OFFSET = 100;

    protected RadioGroup radioGroup;
    protected String selectionStateKey;

    protected SelectOneInputDialog(M viewModel, View view, MaterialAlertDialogBuilder dialogBuilder,
                                   RadioGroup radioGroup,
                                   InputListener<Integer, M> inputListener,
                                   String selectionStateKey,
                                   String shownStateKey,
                                   String inputListenerKey) {
        super(viewModel, view, dialogBuilder, inputListener, shownStateKey, inputListenerKey);
        this.radioGroup = Objects.requireNonNull(radioGroup);
        this.selectionStateKey = Objects.requireNonNull(selectionStateKey);
    }

    @Nullable
    @Override
    public Integer getValue() {
        RadioGroup radioGroup = this.radioGroup;
        return radioGroup != null ? idToIndex(radioGroup.getCheckedRadioButtonId()) : null;
    }

    @Override
    public void setValue(Integer index) {
        RadioGroup radioGroup = this.radioGroup;
        if (radioGroup != null)
            radioGroup.check(indexToId(index));
    }

    @Override
    public void saveState(@Nullable Bundle outState) {
        super.saveState(outState);
        RadioGroup radioGroup = this.radioGroup;
        if (outState != null && radioGroup != null)
            outState.putInt(selectionStateKey, radioGroup.getCheckedRadioButtonId());
    }

    @Override
    public void dismiss() {
        super.dismiss();
        this.radioGroup = null;
    }

    public static Integer idToIndex(int id) {
        int x = id - ID_OFFSET;
        return x < 0 ? null : x;
    }

    public static int indexToId(Integer index) {
        return (index == null || index < 0) ? -1 : index + ID_OFFSET;
    }

    public static void setSelectionByIndex(RadioGroup radioGroup, Integer index) {
        setSelectionById(radioGroup, indexToId(index));
    }

    public static void setSelectionById(RadioGroup radioGroup, int selectedId) {
        if (radioGroup != null) {
            int size = radioGroup.getChildCount();
            int index = 0;
            for (int i = 0; i < size; i++) {
                View child = radioGroup.getChildAt(i);
                if (child instanceof Checkable) {
                    ((Checkable) child).setChecked(child.getId() == selectedId);
                    index++;
                }
            }
        }
    }

    public static class Builder<VM> extends InputDialog.Builder<Integer, VM, SelectOneInputDialog<VM>> {

        @ArrayRes
        protected int options;
        @ArrayRes
        protected Integer descriptions;

        protected Integer defaultValue;
        protected String selectionStateKey;

        public Builder(@NonNull Context context, @ArrayRes int options, @NonNull String name) {
            super(context, name);
            this.selectionStateKey = getInstanceNamespace(name) + ".selection";
            this.options = options;
        }

        public Builder<VM> setDescriptions(Integer descriptions) {
            this.descriptions = descriptions;
            return this;
        }

        public Builder<VM> setDefaultValue(LongPreference defaultValuePreference) {
            return setDefaultValue(appConfig.getAsInt(defaultValuePreference));
        }

        public Builder<VM> setDefaultValue(Integer index) {
            this.defaultValue = index;
            return this;
        }

        @Override
        public DialogSupplier<SelectOneInputDialog<VM>> loadState(@NonNull VM listenerContext,
                                                                  @NonNull View view,
                                                                  @Nullable Bundle savedState) {
            if (savedState != null)
                defaultValue = idToIndex(savedState.getInt(selectionStateKey, indexToId(defaultValue)));
            super.loadState(listenerContext, view, savedState);
            return this;
        }

        @Override
        @NonNull
        public SelectOneInputDialog<VM> build() {

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

            RadioGroup radioGroup = newInput(padding);
            ScrollView scrollView = new ScrollView(context);
            scrollView.addView(radioGroup);
            dialogLayout.addView(scrollView);

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

            return new SelectOneInputDialog<>(listenerContext, view, builder, radioGroup, inputListener,
                    selectionStateKey, shownStateKey, inputListenerKey);
        }

        private RadioGroup newInput(int padding) {
            RadioGroup radioGroup = new RadioGroup(context);
            radioGroup.setPaddingRelative(padding, 0, padding * 2, 0);
            return populateRadioGroup(context, radioGroup, padding, options, descriptions, defaultValue);
        }

        @NotNull
        public static RadioGroup populateRadioGroup(@NonNull Context context, @NonNull RadioGroup radioGroup, int padding,
                                                    int options, @Nullable Integer descriptions, Integer defaultValue) {
            Resources resources = context.getResources();
            String[] descriptionsText = descriptions != null ? resources.getStringArray(descriptions) : null;
            String[] optionsText = resources.getStringArray(options);

            for (int index = 0; index < optionsText.length; index++) {
                MaterialRadioButton radioButton = new MaterialRadioButton(context);
                radioButton.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
                radioButton.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Body1);
                radioButton.setId(indexToId(index));
                String optionText = optionsText[index];
                if (descriptionsText != null && index < descriptionsText.length) {
                    SpannableStringBuilder label = new SpannableStringBuilder();
                    label.append(optionText);
                    label.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    label.append(StringUtil.NEW_LINE);
                    int start = label.length();
                    label.append(descriptionsText[index]);
                    int length = label.length();
//                    label.setSpan(new StyleSpan(Typeface.ITALIC), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    radioButton.setText(label, TextView.BufferType.SPANNABLE);
                } else
                    radioButton.setText(optionText);

                radioButton.setPadding(0, padding, 0, padding);
                int startOffset = radioButton.getCompoundPaddingStart();
                if (index > 0) {
                    MaterialDivider divider = new MaterialDivider(context);
                    divider.setDividerInsetStart(startOffset);
                    radioGroup.addView(divider);
                }

                radioGroup.addView(radioButton);
            }

            radioGroup.check(indexToId(defaultValue));
            return radioGroup;
        }
    }
}
