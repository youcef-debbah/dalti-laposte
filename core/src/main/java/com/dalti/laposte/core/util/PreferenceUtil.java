package com.dalti.laposte.core.util;

import android.content.Context;
import android.text.InputType;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.ActivationState;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.BooleanPreference;
import com.dalti.laposte.core.repositories.IntegerSetting;
import com.dalti.laposte.core.repositories.StringNumberSetting;
import com.dalti.laposte.core.repositories.StringPreference;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

public final class PreferenceUtil {
    public static Preference.OnPreferenceChangeListener INTEGER_PREFERENCE_VALIDATOR =
            (preference1, newValue) -> {
                boolean accepted = StringUtil.parseInteger(StringUtil.toString(newValue)) != null;
                if (!accepted)
                    QueueUtils.toast(R.string.input_invalid);
                return accepted;
            };

    public static EditTextPreference.OnBindEditTextListener NUMERIC_PREFERENCE_BINDER =
            editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER);

    private PreferenceUtil() {
        throw new IllegalAccessError();
    }

    @NotNull
    public static SwitchPreferenceCompat newSwitchPreference(Context context, BooleanPreference preference) {
        SwitchPreferenceCompat switchPreference = new SwitchPreferenceCompat(context);
        switchPreference.setDefaultValue(preference.getDefaultBoolean());
        switchPreference.setKey(preference.name());
        switchPreference.setSwitchTextOn(R.string.on);
        switchPreference.setSwitchTextOff(R.string.off);
        return switchPreference;
    }

    @NotNull
    public static SeekBarPreference newSeekBarPreference(Context context, IntegerSetting setting) {
        SeekBarPreference preference = new SeekBarPreference(context);
        preference.setDefaultValue(setting.getDefaultInteger());
        preference.setMin(setting.getMinValue());
        preference.setMax(setting.getMaxValue());
        preference.setKey(setting.name());
        preference.setAdjustable(true);
        preference.setSeekBarIncrement(setting.getStep());
        preference.setOnPreferenceClickListener(clickedPreference -> {
            preference.setValue(setting.getDefaultInteger());
            return true;
        });
        preference.setOnPreferenceChangeListener((updatedPreference, newValue) -> {
            if (newValue instanceof Integer) {
                int newInt = (Integer) newValue;
                preference.setValue(newInt - newInt % setting.getStep());
            }
            return false;
        });
        return preference;
    }

    @NotNull
    public static EditTextPreference newStringNumberPreference(Context context, StringNumberSetting setting) {
        EditTextPreference preference = new EditTextPreference(context);
        preference.setKey(setting.name());
        preference.setDefaultValue(setting.getDefaultString());
        preference.setOnBindEditTextListener(NUMERIC_PREFERENCE_BINDER);
        preference.setSummaryProvider(new NumberSummary(setting));
        preference.setOnPreferenceChangeListener(INTEGER_PREFERENCE_VALIDATOR);
        return preference;
    }

    @NotNull
    public static ListPreference newListPreference(Context context, int entries, int values, StringPreference stringPreference) {
        ListPreference preference = new ListPreference(context);
        preference.setKey(stringPreference.name());
        preference.setEntries(entries);
        preference.setEntryValues(values);
        preference.setDefaultValue(stringPreference.getDefaultString());
        preference.setSummaryProvider(s -> preference.getEntry());
        return preference;
    }

    @NotNull
    public static Preference getStaticTextPreference(Context context, int title, String value) {
        Preference preference = new Preference(context);
        preference.setPersistent(false);
        preference.setTitle(title);
        preference.setDefaultValue(value);
        preference.setSummary(value);
        preference.setCopyingEnabled(true);
        return preference;
    }

    @NotNull
    public static Preference getStaticTextPreference(Context context, int title, StringPreference setting) {
        return getStaticTextPreference(context, title, AppConfig.getInstance().get(setting));
    }

    public static Preference getActivationStatePreference(Context context) {
        return getStaticTextPreference(context, R.string.activation_state, getActivationStateAsString(context));
    }

    public static String getActivationStateAsString(Context context) {
        AppConfig appConfig = AppConfig.getInstance();
        ActivationState activationState = appConfig.getActivationState();
        Long expirationDate = activationState.getExpirationDate();
        if (activationState.isActive())
            return expirationDate == null ? context.getString(R.string.activated) : context.getString(R.string.activated_until, TimeUtils.formatAsDateTime(expirationDate));
        else
            return expirationDate == null ? context.getString(R.string.not_activated_yet) : context.getString(R.string.activation_expired_since, TimeUtils.formatAsDateTime(expirationDate));
    }

    public static class NumberSummary implements Preference.SummaryProvider<EditTextPreference> {

        private static final NumberFormat FORMAT = NumberFormat.getNumberInstance();

        private final StringNumberSetting setting;

        public NumberSummary(StringNumberSetting setting) {
            this.setting = setting;
        }

        @Override
        public CharSequence provideSummary(EditTextPreference preference) {
            return FORMAT.format(StringUtil.parseLong(preference.getText(), setting.getDefaultLong()));
        }
    }
}
