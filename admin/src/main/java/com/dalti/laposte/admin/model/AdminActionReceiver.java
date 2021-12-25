package com.dalti.laposte.admin.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.dalti.laposte.admin.ui.CompactDashboardService;
import com.dalti.laposte.core.model.BasicActionReceiver;
import com.dalti.laposte.core.repositories.Event;
import com.dalti.laposte.core.repositories.SmsRepository;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.Request;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.backend.Item;

@AndroidEntryPoint
public class AdminActionReceiver extends BasicActionReceiver {

    @Inject
    SmsRepository smsRepository;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (context != null && intent != null) {
            String action = intent.getStringExtra(BasicActionReceiver.ACTION_KEY);
            if (Request.HIDE_COMPACT_UI.name().equals(action)) {
                context.stopService(new Intent(context, CompactDashboardService.class));
                Teller.log(Event.HideCompactDashboard.NAME, Event.HideCompactDashboard.Param.COMPACT_UI_HIDING_TRIGGER, Event.Trigger.CLICK_NOTIFICATION_ACTION);
            } else if (smsRepository != null) {
                int resultCode = getResultCode();
                String smsToken = intent.getStringExtra(SmsRepository.SMS_TOKEN);
                long id = intent.getLongExtra(SmsRepository.SMS_ID, Item.AUTO_ID);
                if (Request.COUNT_SMS_AS_SENT.name().equals(action)) {
                    if (resultCode == Activity.RESULT_OK) {
                        smsRepository.confirmSmsSentSuccessfully(id, smsToken);
                        if (intent.getBooleanExtra(SmsRepository.SMS_SUPPORT_RESENDING, false))
                            SmsRepository.scheduleResend(smsToken, intent.getStringExtra(SmsRepository.SMS_PHONE), intent.getStringExtra(SmsRepository.SMS_TEXT_CONTENT));
                    } else
                        smsRepository.confirmSmsSkip(id, smsToken, resultCode);
                } else if (Request.COUNT_SMS_AS_DELIVERED.name().equals(action)) {
                    if (resultCode == Activity.RESULT_OK) {
                        smsRepository.countDelivered(id, smsToken);
                        SmsRepository.cancelResend(smsToken);
                    }
                }
            }
        }
    }
}
