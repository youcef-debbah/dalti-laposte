package com.dalti.laposte.client.ui;


import android.content.Context;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.dalti.laposte.client.R;
import com.dalti.laposte.client.repository.ClientBuildConfiguration;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.BooleanSetting;
import com.dalti.laposte.core.repositories.IntegerSetting;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.StringNumberSetting;
import com.dalti.laposte.core.util.PreferenceUtil;
import com.dalti.laposte.core.util.QueueUtils;

import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

public class ClientSettingsFragment extends PreferenceFragmentCompat {

    private final ClientBuildConfiguration buildConf = new ClientBuildConfiguration();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = requireContext();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        PreferenceCategory notificationCategory = new PreferenceCategory(context);
        if (preferenceScreen.addPreference(notificationCategory)) {
            notificationCategory.setTitle(R.string.feedback_settings);

            SwitchPreferenceCompat unknownAvailabilityNotification = PreferenceUtil.newSwitchPreference(context, BooleanSetting.UNKNOWN_AVAILABILITY_NOTIFICATION);
            if (notificationCategory.addPreference(unknownAvailabilityNotification)) {
                unknownAvailabilityNotification.setTitle(R.string.unknown_availability_notification_title);
                unknownAvailabilityNotification.setSummary(R.string.unknown_availability_notification_summary);
            }

            SwitchPreferenceCompat toastFromBackground = PreferenceUtil.newSwitchPreference(context, BooleanSetting.TOAST_FROM_BACKGROUND);
            if (notificationCategory.addPreference(toastFromBackground)) {
                toastFromBackground.setTitle(R.string.toast_from_background_title);
                toastFromBackground.setSummary(R.string.toast_from_background_summery);
            }
        }

        PreferenceCategory scannerCategory = new PreferenceCategory(context);
        if (preferenceScreen.addPreference(scannerCategory)) {
            scannerCategory.setTitle(R.string.scanner);

            final SwitchPreferenceCompat scannerFlashLight = PreferenceUtil.newSwitchPreference(context, BooleanSetting.SCANNER_FLASH_LIGHT);
            if (scannerCategory.addPreference(scannerFlashLight)) {
                scannerFlashLight.setTitle(R.string.scanner_flash_light);
                scannerFlashLight.setSummary(R.string.scanner_flash_light_summary);
            }

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

        AppConfig appConfig = AppConfig.getInstance();
        PreferenceCategory aboutCategory = new PreferenceCategory(context);
        if (preferenceScreen.addPreference(aboutCategory)) {
            aboutCategory.setTitle(R.string.about);

            aboutCategory.addPreference(PreferenceUtil.getActivationStatePreference(context));
            if (QueueUtils.isTesting()) {
                final String lastUpdate = TimeUtils.formatAsDateTime(appConfig.getLong(LongSetting.LAST_AUTO_REFRESH));
                aboutCategory.addPreference(PreferenceUtil.getStaticTextPreference(context, R.string.last_auto_refresh, lastUpdate != null ? lastUpdate : GlobalConf.EMPTY_TOKEN));
            }
            aboutCategory.addPreference(PreferenceUtil.getStaticTextPreference(context, R.string.version, buildConf.getFullVersionName()));
            aboutCategory.addPreference(PreferenceUtil.getStaticTextPreference(context, R.string.in_app_messages_id, appConfig.getInAppMessageID()));
            aboutCategory.addPreference(PreferenceUtil.getStaticTextPreference(context, R.string.google_services_id, appConfig.getApplicationID()));
        }
    }
}
