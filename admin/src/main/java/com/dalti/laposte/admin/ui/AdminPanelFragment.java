package com.dalti.laposte.admin.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Checkable;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.Event;
import com.dalti.laposte.core.repositories.LiveLongProperty;
import com.dalti.laposte.core.repositories.LiveStringProperty;
import com.dalti.laposte.core.repositories.SmsRepository;
import com.dalti.laposte.core.repositories.StringSetting;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.AbstractQueueFragment;
import com.dalti.laposte.core.ui.DialogSupplier;
import com.dalti.laposte.core.ui.TextInputDialog;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.common.InputListener;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

@AndroidEntryPoint
public class AdminPanelFragment extends AbstractQueueFragment {

    private ActivityResultLauncher<Intent> overlayPermissionRequest;
    private ActivityResultLauncher<String[]> smsPermissionRequest;

    private DialogSupplier<TextInputDialog<SmsRepository>> phoneDialog;

    @Inject
    SmsRepository smsRepository;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final ViewDataBinding binding = BindingUtil.inflate(this, inflater, R.layout.fragment_admin_panel, container);
        binding.setVariable(BR.fragment, this);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overlayPermissionRequest = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            FragmentActivity context = requireActivity();
            if (ContextUtils.canDrawOverlays(context))
                startCompactDashboardService(context);
            else
                QueueUtils.showToast(requireContext(), R.string.overlay_permission_needed);
        });

        smsPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> result) -> {
            if (result != null)
                for (Map.Entry<String, Boolean> entry : result.entrySet())
                    if (!StringUtil.isTrue(entry.getValue())) {
                        smsRepository.disableShortMessagesRequests();
                        Teller.logPermissionDenied(entry.getKey());
                        return;
                    }

            smsRepository.enableShortMessagesRequests();
        });
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        phoneDialog = new TextInputDialog.Builder<SmsRepository>(requireContext(), "phone")
                .setHint(R.string.phone_number)
                .setPrefix(R.string.phone_prefix)
                .setCounter(9)
                .setLayoutDirection(View.LAYOUT_DIRECTION_LTR)
                .setInputType(EditorInfo.TYPE_CLASS_PHONE)
                .setImeOptions(EditorInfo.IME_ACTION_SEND)
                .setKeyListener(DigitsKeyListener.getInstance("0123456789"))
                .setTitle(R.string.send_test_sms)
                .loadState(smsRepository, view, savedInstanceState)
        ;
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        DialogSupplier.saveDialog(phoneDialog, outState);
        super.onSaveInstanceState(outState);
    }

    public void openActivationsList(View view) {
        startActivity(ActivationsListActivity.class);
    }

    public void openAdminAlarmsList(View view) {
        startActivity(AdminAlarmListActivity.class);
    }

    public void testSMS(View v) {
        DialogSupplier.showDialog(AppConfig.getInstance().get(StringSetting.TEST_PHONE_NUMBER), phoneDialog, newPhoneInputListener(smsRepository));
    }

    private static InputListener<String, SmsRepository> newPhoneInputListener(SmsRepository smsRepository) {
        return (context, view, input) -> {
            if (input != null) {
                AppConfig.getInstance().put(StringSetting.TEST_PHONE_NUMBER, input);
                context.testSMS(QueueUtils.getString(R.string.phone_prefix) + input);
            }
        };
    }

    public void stopCompactUI() {
        FragmentActivity activity = requireActivity();
        activity.stopService(new Intent(activity, CompactDashboardService.class));
        Teller.log(Event.HideCompactDashboard.NAME, Event.HideCompactDashboard.Param.COMPACT_UI_HIDING_TRIGGER, Event.Trigger.CLICK_CONTROLLER);
    }

    public void startCompactUI() {
        Activity activity = requireActivity();
        if (ContextUtils.canDrawOverlays(activity))
            startCompactDashboardService(activity);
        else
            overlayPermissionRequest.launch(
                    new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + activity.getPackageName()))
            );
    }

    public LiveData<Boolean> getSmsSendingEnabled() {
        return smsRepository.getSmsSendingEnabled();
    }

    public void toggleSmsState(View v) {
        if (v instanceof Checkable && ((Checkable) v).isChecked()) {
            if (ContextUtils.isPermissionsGranted(requireContext(), SmsRepository.SMS_SENDING_PERMISSIONS))
                smsRepository.enableShortMessagesRequests();
            else
                smsPermissionRequest.launch(SmsRepository.SMS_SENDING_PERMISSIONS);
        } else
            smsRepository.disableShortMessagesRequests();
    }

    public LiveData<Boolean> isCompactUIShown() {
        return AppConfig.getInstance().getCompactDashboardShown();
    }

    public void toggleCompactUI(View v) {
        if (v instanceof Checkable && ((Checkable) v).isChecked())
            startCompactUI();
        else
            stopCompactUI();
    }

    public void startCompactDashboardService(Activity activity) {
        boolean serviceFound = startServiceThenUpgradeToForeground(CompactDashboardService.class);
        if (serviceFound)
            Teller.log(Event.ShowCompactDashboard.NAME, Event.ShowCompactDashboard.Param.COMPACT_UI_SHOWING_TRIGGER, Event.Trigger.CLICK_CONTROLLER);
        else {
            QueueUtils.toast(R.string.could_not_start_service);
            Teller.logUnexpectedCondition();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DialogSupplier.dismissDialog(phoneDialog);
    }

    public LiveLongProperty getSmsRequests() {
        return smsRepository.getSmsRequestsCounter();
    }

    public LiveLongProperty getSentMessages() {
        return smsRepository.getSmsSentCounter();
    }

    public LiveLongProperty getDeliveredMessages() {
        return smsRepository.getSmsDeliveredCounter();
    }

    public LiveStringProperty getSmsLatestOutcome() {
        return smsRepository.getSmsLatestOutcome();
    }

    public LiveLongProperty getSmsIgnored() {
        return smsRepository.getSmsIgnoredCounter();
    }

    public LiveLongProperty getSmsFailedMessages() {
        return smsRepository.getSmsFailedCounter();
    }

    public void resetStats(View view) {
        smsRepository.resetCounters();
    }

    public void openStatsActivity(View v) {
        startActivity(ShortMessagesStatsActivity.class);
    }

    public String getNamespace() {
        return "admin_panel_fragment";
    }
}
