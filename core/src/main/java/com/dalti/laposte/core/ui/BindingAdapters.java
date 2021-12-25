package com.dalti.laposte.core.ui;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.InputProperty;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.repositories.Validity;
import com.dalti.laposte.core.util.Dimension;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

@BindingMethods({
        @BindingMethod(type = ImageView.class, attribute = "srcCompat", method = "setImageDrawable")
})
public class BindingAdapters {

    @ColorRes
    public static final int DEFAULT_TINT_COLOR = R.color.on_surface_extra_light_color_selector;
    public static final String NUMBER_FORMAT_03D = "%03d";
    public static final String NUMBER_FORMAT_02D = "%02d";

    @BindingAdapter("layout_percent_margin")
    public static void setMargin(View view, Object value) {
        Integer percent = GlobalUtil.bound(StringUtil.parseInteger(StringUtil.toString(value)), 0, 50);
        if (percent != null && view != null && view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            Context context = view.getContext();
            if (context instanceof Activity) {
                Dimension dim = QueueUtils.getDisplaySize((Activity) context);
                if (dim != null) {
                    int margin = (dim.getMin() * percent) / 100;
                    ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).setMargins(margin, margin, margin, margin);
                    view.requestLayout();
                }
            }
            view.getViewTreeObserver().addOnGlobalLayoutListener(new MarginHandler(view, percent));
        }
    }

    private static final class MarginHandler implements ViewTreeObserver.OnGlobalLayoutListener {

        final View view;
        final int percent;

        public MarginHandler(View view, int percent) {
            this.view = Objects.requireNonNull(view);
            this.percent = percent;
        }

        @Override
        public void onGlobalLayout() {
            View view = this.view;
            View parent = getParent(view);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            int margin = (Math.min(parent.getWidth(), parent.getHeight()) * percent) / 100;
            if (params.leftMargin != margin || params.topMargin != margin
                    || params.rightMargin != margin || params.bottomMargin != margin) {
                params.setMargins(margin, margin, margin, margin);
                view.requestLayout();
            }
        }

        private View getParent(View view) {
            if (view == null)
                return null;
            else {
                ViewParent parent = view.getParent();
                if (parent instanceof View)
                    return (View) parent;
                else
                    return view;
            }
        }
    }

    @BindingAdapter("date_time")
    public static void setDateTime(TextView view, Object object) {
        Long epoch = StringUtil.parseAsLong(object);
        setString(view, epoch != null ? TimeUtils.formatAsDateTime(epoch) : null);
    }

    @BindingAdapter("date_only")
    public static void setDateOnly(TextView view, Object object) {
        Long epoch = StringUtil.parseAsLong(object);
        setString(view, epoch != null ? TimeUtils.formatAsDate(epoch) : null);
    }

    @BindingAdapter("time_only")
    public static void setTimeOnly(TextView view, Object object) {
        Long epoch = StringUtil.parseAsLong(object);
        setString(view, epoch != null ? TimeUtils.formatAsTime(epoch) : null);
    }

    @BindingAdapter("duration_sec_min")
    public static void setDurationSecMin(TextView view, Object object) {
        Long epoch = StringUtil.parseAsLong(object);
        setString(view, epoch != null ? QueueUtils.formatAsDurationOfSecMin(epoch) : null);
    }

    @BindingAdapter("duration_sec_min_compact")
    public static void setDurationSecMinCompact(TextView view, Object object) {
        Long epoch = StringUtil.parseAsLong(object);
        setString(view, epoch != null ? QueueUtils.formatAsDurationOfSecMinCompact(epoch) : null);
    }

    @BindingAdapter("duration_min_hour")
    public static void setDurationMinHour(TextView view, Object object) {
        Long epoch = StringUtil.parseAsLong(object);
        setString(view, epoch != null ? QueueUtils.formatAsDurationOfMinHour(epoch) : null);
    }

    @BindingAdapter("live_string")
    public static void setLiveString(TextView view, LiveData<?> data) {
        if (data == null)
            setString(view, null);
        else {
            Context context = view.getContext();
            if (context instanceof LifecycleOwner)
                data.observe((LifecycleOwner) context, x -> setString(view, x));
        }
    }

    @BindingAdapter("string")
    public static void setString(TextView view, Object object) {
        String stringValue = StringUtil.toString(object);
        if (StringUtil.isBlank(stringValue))
            view.setText(QueueUtils.getString(R.string.unknown_symbol));
        else
            view.setText(stringValue);
    }

    @BindingAdapter("formatted_number")
    public static void setFormattedNumber(TextView view, Object object) {
        Long number = StringUtil.parseAsLong(object);
        if (number != null)
            view.setText(view.getContext().getString(R.string.number_value, number));
        else
            view.setText(QueueUtils.getString(R.string.unknown_symbol));
    }

    @BindingAdapter("string_res")
    public static void setStringRes(TextView view, Object object) {
        if (object instanceof CharSequence) {
            String value = object.toString();
            view.setVisibility(StringUtil.isBlank(value) ? View.GONE : View.VISIBLE);
            view.setText(value);
        } else {
            Integer stringRes = StringUtil.parseAsInteger(object);
            if (ContextUtils.isValidID(stringRes)) {
                view.setText(stringRes);
                view.setVisibility(View.VISIBLE);
            } else {
                view.setText(R.string.unknown_symbol);
                view.setVisibility(View.GONE);
            }
        }
    }

    @BindingAdapter("text_color_list")
    public static void setTextColorStateList(TextView view, Object object) {
        ColorStateList colorStateList = getColorStateList(view.getContext(), object);
        if (colorStateList != null)
            view.setTextColor(colorStateList);
    }

    private static ColorStateList getColorStateList(Context context, Object input) {
        if (input instanceof ColorStateList)
            return (ColorStateList) input;

        Integer colorStateListID = StringUtil.parseAsInteger(input);
        if (!ContextUtils.isValidID(colorStateListID))
            colorStateListID = DEFAULT_TINT_COLOR;

        return AppCompatResources.getColorStateList(context, colorStateListID);
    }

    @BindingAdapter("src_res")
    public static void setSrcRes(ImageView view, Object srcInput) {
        Drawable drawable = getDrawable(view.getContext(), srcInput);
        if (drawable != null) {
            view.setVisibility(View.VISIBLE);
            view.setImageDrawable(drawable);
        } else {
            view.setVisibility(View.GONE);
            view.setImageDrawable(null);
        }
    }

    @BindingAdapter("icon_res")
    public static void setIconRes(View view, Object srcInput) {
        if (view instanceof MaterialButton) {
            MaterialButton button = (MaterialButton) view;
            button.setIcon(getDrawable(button.getContext(), srcInput));
        }
    }

    @BindingAdapter(value = {"src_res_tinted", "tint_color_list"}, requireAll = false)
    public static void setSrcResTinted(ImageView view, Object srcInput, Object tintInput) {
        Drawable drawable = getDrawable(view.getContext(), srcInput);
        if (drawable != null) {
            view.setVisibility(View.VISIBLE);
            view.setImageDrawable(tintDrawable(drawable, getColorStateList(view.getContext(), tintInput)));
        } else {
            view.setVisibility(View.GONE);
            view.setImageDrawable(null);
        }
    }

    private static Drawable getDrawable(Context context, Object input) {
        Objects.requireNonNull(context);
        if (input instanceof Drawable)
            return (Drawable) input;
        else {
            Integer drawableID = StringUtil.parseAsInteger(input);
            if (ContextUtils.isValidID(drawableID))
                return AppCompatResources.getDrawable(context, drawableID);
            else
                return null;
        }
    }

    private static Drawable tintDrawable(Drawable drawableInput, ColorStateList colorStateList) {
        if (drawableInput != null && colorStateList != null) {
            Drawable drawable = DrawableCompat.wrap(drawableInput);
            DrawableCompat.setTintList(drawable, colorStateList);
            return drawable;
        } else
            return drawableInput;
    }

    @BindingAdapter("background_res")
    public static void setBackgroundRes(ImageView view, Object object) {
        if (object instanceof Drawable) {
            view.setBackground((Drawable) object);
        } else {
            Integer drawableID = StringUtil.parseAsInteger(object);
            if (ContextUtils.isValidID(drawableID))
                view.setBackgroundResource(drawableID);
            else
                view.setBackground(null);
        }
    }

    @BindingAdapter("visible_unless")
    public static void setVisibleUnless(View view, Object value) {
        Integer visibility = StringUtil.parseAsInteger(value);
        if (StringUtil.isZero(visibility))
            view.setVisibility(View.VISIBLE);
        else
            ContextUtils.setVisibility(view, visibility);
    }

    @BindingAdapter("invisible_unless")
    public static void setInvisibleUnless(View view, Object value) {
        Integer visibility = StringUtil.parseAsInteger(value);
        if (StringUtil.isZero(visibility))
            view.setVisibility(View.INVISIBLE);
        else
            ContextUtils.setVisibility(view, visibility);
    }

    @BindingAdapter("visible_on_data")
    public static void setVisibleOnData(View view, Object value) {
        if (isEmpty(value))
            view.setVisibility(View.GONE);
        else
            view.setVisibility(View.VISIBLE);
    }

//    @BindingAdapter("gone_on_data")
//    public static void setGoneOnData(View view, Object value) {
//        if (isEmpty(value))
//            view.setVisibility(View.VISIBLE);
//        else
//            view.setVisibility(View.GONE);
//    }

    private static boolean isEmpty(Object value) {
        if (value == null)
            return true;

        Long number = StringUtil.parseAsLong(value);
        return (number != null && number == 0L)
                || (value instanceof Collection && ((Collection<?>) value).isEmpty())
                || StringUtil.isBlank(StringUtil.toString(value));
    }

    @BindingAdapter("gone_unless_has_children")
    public static void setGoneUnlessHasChildren(ViewGroup view, Object value) {
        Integer minChildrenCount = StringUtil.parseAsInteger(value);
        if (minChildrenCount != null && countVisibleChildren(view) >= minChildrenCount)
            view.setVisibility(View.VISIBLE);
        else
            view.setVisibility(View.GONE);
    }

    private static int countVisibleChildren(ViewGroup view) {
        int count = 0;
        int total = view.getChildCount();
        for (int i = 0; i < total; i++) {
            View child = view.getChildAt(i);
            if (child != null && child.getVisibility() != View.GONE)
                count++;
        }
        return count;
    }

    @BindingAdapter("gone_unless")
    public static void setGoneUnless(View view, Object value) {
        Integer visibility = StringUtil.parseAsInteger(value);
        if (StringUtil.isZero(visibility))
            view.setVisibility(View.GONE);
        else
            ContextUtils.setVisibility(view, visibility);
    }

    @BindingAdapter("number_03d")
    public static void setNumber3Digits(TextView view, Object object) {
        Long number = StringUtil.parseAsLong(object);
        if (number == null)
            view.setText(QueueUtils.getString(R.string.unknown_symbol));
        else
            view.setText(String.format(Locale.getDefault(), NUMBER_FORMAT_03D, number));
    }

    @BindingAdapter("duration_hh_mn")
    public static void set(ViewGroup viewGroup, Object object) {
        Long millis = StringUtil.parseAsLong(object);
        Context context = viewGroup.getContext();
        viewGroup.removeAllViews();

        if (millis == null || millis < TimeUtils.ONE_SECOND_MILLIS)
            viewGroup.setVisibility(View.GONE);
        else {
            viewGroup.setVisibility(View.VISIBLE);
            if (millis < TimeUtils.ONE_MINUTE_MILLIS) {
                MaterialTextView label = new MaterialTextView(context);
                label.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Clock);
                label.setText(R.string.less_than_1min);
                viewGroup.addView(label);
            } else
                addTimeLabels(context, viewGroup, millis);
        }
    }

    private static void addTimeLabels(Context context, ViewGroup viewGroup, Long millis) {
        int hours = (int) TimeUnit.MILLISECONDS.toHours(millis);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hours));

        MaterialTextView hoursView = new MaterialTextView(context);
        hoursView.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Clock_Number);
        hoursView.setText(context.getString(R.string.number_value, hours));

        MaterialTextView hoursUnit = new MaterialTextView(context);
        hoursUnit.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Clock_Label);
        hoursUnit.setText(context.getResources().getString(R.string.hr));

        MaterialTextView minutesView = new MaterialTextView(context);
        minutesView.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Clock_Number);
        minutesView.setText(context.getString(R.string.number_value, minutes));

        MaterialTextView minutesUnit = new MaterialTextView(context);
        minutesUnit.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Clock_Label);
        minutesUnit.setText(context.getResources().getString(R.string.min));

        int padding = context.getResources().getDimensionPixelOffset(R.dimen.tiny);
        MaterialTextView separator = new MaterialTextView(context);
        separator.setPadding(padding, padding, padding, padding);
        separator.setTextAppearance(context, R.style.TextAppearance_Jsoftware95_Clock);
        separator.setText(R.string.time_separator);

        viewGroup.addView(hoursView);
        viewGroup.addView(hoursUnit);
        viewGroup.addView(separator);
        viewGroup.addView(minutesView);
        viewGroup.addView(minutesUnit);
    }

    @BindingAdapter("min_value")
    public static void setMinValue(View numberPicker, Object object) {
        Integer value = StringUtil.parseAsInteger(object);
        if (value != null && numberPicker instanceof NumberPicker)
            ((NumberPicker) numberPicker).setMinValue(value);
    }

    @BindingAdapter("max_value")
    public static void setMaxValue(View numberPicker, Object object) {
        Integer value = StringUtil.parseAsInteger(object);
        if (value != null && numberPicker instanceof NumberPicker)
            ((NumberPicker) numberPicker).setMaxValue(value);
    }

    @SuppressLint("SetTextI18n")
    @BindingAdapter("phone_formatted")
    public static void setPhoneFormatted(TextView view, Object object) {
        if (object instanceof CharSequence) {
            String phone = object.toString();
            String phonePrefix = view.getContext().getString(R.string.phone_prefix);
            String phoneNumber = phone.startsWith(phonePrefix) ? phone.substring(phonePrefix.length()) : phone;
            view.setText(phonePrefix + GlobalUtil.addSpaces(phoneNumber, 2));
            view.setVisibility(View.VISIBLE);
        } else
            setStringRes(view, object);
    }

    @BindingAdapter("phone")
    public static void setPhone(TextView view, Object object) {
        String input = StringUtil.toString(object);
        String phonePrefix = view.getContext().getString(R.string.phone_prefix);
        String phoneInput = input != null && input.startsWith(phonePrefix) ? input.substring(phonePrefix.length()) : input;
        view.setText(phoneInput);
        switch (InputProperty.validatePhoneInput(phoneInput)) {
            case TOO_SHORT:
                setError(view, Validity.TOO_SHORT);
                break;
            case TOO_LONG:
                setError(view, Validity.TOO_LONG);
                break;
            case INVALID:
                view.setError(QueueUtils.getString(R.string.invalid_phone));
                break;
            default:
                view.setError(null);
        }
    }

    public static void setError(TextView view, Validity validity) {
        view.setError(QueueUtils.getString(validity.getMessage()));
    }

    @BindingAdapter("controller_name")
    public static void setControllerName(View view, Object tag) {
        view.setTag(R.id.controller_name, String.valueOf(tag));
    }

    @BindingAdapter("controller_payload")
    public static void setControllerPayload(View view, Object tag) {
        view.setTag(R.id.controller_payload, String.valueOf(tag));
    }

    @BindingAdapter("on_click")
    public static void setOnClick(View view, View.OnClickListener listener) {
        if (listener != null)
            view.setOnClickListener(new OnClickListenerWrapper(listener));
        else
            view.setOnClickListener(null);
    }

    @BindingAdapter("animate_layout")
    public static void setAnimeLayout(View view, Object value) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (StringUtil.isTrue(value))
                viewGroup.setLayoutTransition(newDefaultTransition());
            else
                ((ViewGroup) view).setLayoutTransition(null);
        }
    }

    @NotNull
    public static LayoutTransition newDefaultTransition() {
        LayoutTransition transition = new LayoutTransition();
        transition.setAnimateParentHierarchy(false);
        return transition;
    }

    private static final class OnClickListenerWrapper implements View.OnClickListener {

        final View.OnClickListener source;

        public OnClickListenerWrapper(@NonNull View.OnClickListener source) {
            this.source = Objects.requireNonNull(source);
        }

        @Override
        public void onClick(View v) {
            if (v != null) {
                String name = GlobalUtil.truncate(String.valueOf(v.getTag(R.id.controller_name)), 40);
                String payload = GlobalUtil.truncate(String.valueOf(v.getTag(R.id.controller_payload)), 40);
                Teller.logClick(name, payload);
            }
            source.onClick(v);
        }
    }
}