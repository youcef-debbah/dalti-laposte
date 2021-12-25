package com.dalti.laposte.admin.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.repositories.AdminBuildConfiguration;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.BooleanSetting;
import com.dalti.laposte.core.repositories.IntegerSetting;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.StringNumberSetting;
import com.dalti.laposte.core.repositories.StringSetting;
import com.dalti.laposte.core.util.PreferenceUtil;
import com.dalti.laposte.core.util.QueueUtils;

import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

public class AdminSettingsFragment extends PreferenceFragmentCompat {

    private final AdminBuildConfiguration buildConf = new AdminBuildConfiguration();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = requireContext();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        PreferenceCategory feedbackCategory = new PreferenceCategory(context);
        if (preferenceScreen.addPreference(feedbackCategory)) {
            feedbackCategory.setTitle(R.string.feedback_settings);

            SwitchPreferenceCompat immediateFeedback = PreferenceUtil.newSwitchPreference(context, BooleanSetting.IMMEDIATE_FEEDBACK);
            if (feedbackCategory.addPreference(immediateFeedback)) {
                immediateFeedback.setTitle(R.string.immediate_feedback);
                immediateFeedback.setSummary(R.string.immediate_feedback_summary);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                SwitchPreferenceCompat showCompactUiNotification = PreferenceUtil.newSwitchPreference(context, BooleanSetting.SHOW_COMPACT_UI_NOTIFICATION);
                if (feedbackCategory.addPreference(showCompactUiNotification)) {
                    showCompactUiNotification.setTitle(getString(R.string.compact_dashboard_setting_title));
                    showCompactUiNotification.setSummary(getString(R.string.compact_dashboard_setting_summary));
                }
            }

            SwitchPreferenceCompat toastFromBackground = PreferenceUtil.newSwitchPreference(context, BooleanSetting.TOAST_FROM_BACKGROUND);
            if (feedbackCategory.addPreference(toastFromBackground)) {
                toastFromBackground.setTitle(getString(R.string.toast_from_background_title));
                toastFromBackground.setSummary(getString(R.string.toast_from_background_summery));
            }

            SwitchPreferenceCompat vibrateWhenUpdateSent = PreferenceUtil.newSwitchPreference(context, BooleanSetting.VIBRATE_ON_UPDATE_SENT);
            if (feedbackCategory.addPreference(vibrateWhenUpdateSent)) {
                vibrateWhenUpdateSent.setTitle(R.string.vibrate_on_update_sending);
                vibrateWhenUpdateSent.setSummary(R.string.vibrate_on_update_sending_summary);
            }

            SeekBarPreference vibrationDuration = PreferenceUtil.newSeekBarPreference(context, IntegerSetting.UPDATE_SENT_VIBRATION_DURATION);
            if (feedbackCategory.addPreference(vibrationDuration)) {
                vibrationDuration.setTitle(R.string.vibration_duration);
                vibrationDuration.setSummaryProvider(p -> getString(R.string.n_milliseconds, vibrationDuration.getValue()));
                vibrationDuration.setDependency(BooleanSetting.VIBRATE_ON_UPDATE_SENT.name());
            }
        }

        PreferenceCategory inputCategory = new PreferenceCategory(context);
        if (preferenceScreen.addPreference(inputCategory)) {
            inputCategory.setTitle(context.getText(R.string.input_verification));

            EditTextPreference maxToken = PreferenceUtil.newStringNumberPreference(context, StringNumberSetting.MAX_TOKEN);
            if (inputCategory.addPreference(maxToken)) {
                maxToken.setTitle(context.getString(R.string.max_token));
                maxToken.setDialogTitle(context.getString(R.string.max_token));
            }

            EditTextPreference maxWaiting = PreferenceUtil.newStringNumberPreference(context, StringNumberSetting.MAX_WAITING);
            if (inputCategory.addPreference(maxWaiting)) {
                maxWaiting.setTitle(context.getString(R.string.max_waiting));
                maxWaiting.setDialogTitle(context.getString(R.string.max_waiting));
            }
        }

        PreferenceCategory scannerCategory = new PreferenceCategory(context);
        if (preferenceScreen.addPreference(scannerCategory)) {
            scannerCategory.setTitle(context.getText(R.string.scanner));

            ListPreference cameraSelectList = PreferenceUtil.newListPreference(context, R.array.cameras_names, R.array.cameras_indexes, StringNumberSetting.SCANNING_CAMERA_INDEX);
            if (scannerCategory.addPreference(cameraSelectList)) {
                cameraSelectList.setTitle(R.string.scanning_camera);
                cameraSelectList.setDialogTitle(R.string.select_scanning_camera);
            }

            SeekBarPreference scannerVibrationDuration = PreferenceUtil.newSeekBarPreference(context, IntegerSetting.QR_SCANNER_VIBRATION_DURATION);
            if (scannerCategory.addPreference(scannerVibrationDuration)) {
                scannerVibrationDuration.setTitle(R.string.vibration_duration_on_scan);
                scannerVibrationDuration.setSummaryProvider(p -> getString(R.string.n_milliseconds, scannerVibrationDuration.getValue()));
            }
        }

        PreferenceCategory smsCategory = new PreferenceCategory(context);
        if (preferenceScreen.addPreference(smsCategory)) {
            smsCategory.setTitle(context.getText(R.string.sms_category));

            ListPreference ooredooConfig = PreferenceUtil.newListPreference(context, R.array.sim_strategy_names, R.array.sim_strategy_values, StringNumberSetting.OOREDOO_SMS_CONFIG);
            if (smsCategory.addPreference(ooredooConfig)) {
                ooredooConfig.setTitle(R.string.send_ooredoo_sim_title);
                ooredooConfig.setDialogTitle(R.string.send_ooredoo_sim_dialog_title);
            }

            ListPreference mobilisConfig = PreferenceUtil.newListPreference(context, R.array.sim_strategy_names, R.array.sim_strategy_values, StringNumberSetting.MOBILIS_SMS_CONFIG);
            if (smsCategory.addPreference(mobilisConfig)) {
                mobilisConfig.setTitle(R.string.send_mobilis_sim_title);
                mobilisConfig.setDialogTitle(R.string.send_mobilis_sim_dialog_title);
            }

            ListPreference djezzyConfig = PreferenceUtil.newListPreference(context, R.array.sim_strategy_names, R.array.sim_strategy_values, StringNumberSetting.DJEZZY_SMS_CONFIG);
            if (smsCategory.addPreference(djezzyConfig)) {
                djezzyConfig.setTitle(R.string.send_djezzy_sim_title);
                djezzyConfig.setDialogTitle(R.string.send_djezzy_sim_dialog_title);
            }

            ListPreference otherConfig = PreferenceUtil.newListPreference(context, R.array.sim_strategy_names, R.array.sim_strategy_values, StringNumberSetting.OTHER_SMS_CONFIG);
            if (smsCategory.addPreference(otherConfig)) {
                otherConfig.setTitle(R.string.send_other_sim_title);
                otherConfig.setDialogTitle(R.string.send_other_sim_dialog_title);
            }
        }

        AppConfig appConfig = AppConfig.getInstance();
        PreferenceCategory aboutCategory = new PreferenceCategory(context);
        if (preferenceScreen.addPreference(aboutCategory)) {
            aboutCategory.setTitle(R.string.about);

            aboutCategory.addPreference(PreferenceUtil.getActivationStatePreference(context));

            final String lastUpdate = TimeUtils.formatAsDateTime(appConfig.getLong(LongSetting.LAST_AUTO_REFRESH));
            aboutCategory.addPreference(PreferenceUtil.getStaticTextPreference(context, R.string.last_auto_refresh, lastUpdate != null ? lastUpdate : GlobalConf.EMPTY_TOKEN));

            if (QueueUtils.isTesting())
                aboutCategory.addPreference(PreferenceUtil.getStaticTextPreference(context, R.string.host, getString(R.string.hostname)));

            aboutCategory.addPreference(PreferenceUtil.getStaticTextPreference(context, R.string.version, buildConf.getFullVersionName()));
            aboutCategory.addPreference(PreferenceUtil.getStaticTextPreference(context, R.string.config_version, appConfig.getRemoteString(StringSetting.REMOTE_CONFIG_VERSION)));
            aboutCategory.addPreference(PreferenceUtil.getStaticTextPreference(context, R.string.in_app_messages_id, appConfig.getInAppMessageID()));
            aboutCategory.addPreference(PreferenceUtil.getStaticTextPreference(context, R.string.google_services_id, appConfig.getApplicationID()));
        }
    }

}
