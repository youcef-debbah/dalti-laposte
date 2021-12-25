package com.dalti.laposte.core.repositories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dalti.laposte.R;
import com.dalti.laposte.core.util.QueueUtils;

import java.util.Objects;

import dz.jsoftware95.queue.common.GlobalUtil;

public class SimStrategy {

    public static final int IGNORE = 0;
    public static final int SIM1_ONLY = 1;
    public static final int SIM2_ONLY = 2;
    public static final int SIM1_SIM2 = 5;
    public static final int SIM2_SIM1 = 6;

    public static final int DEFAULT_STRATEGY = SIM2_ONLY;
    public static final int DEFAULT_STRATEGY_LABEL = R.string.default_strategy;

    private final Integer subscription;
    private final String phone;
    private final boolean deliverable;

    public SimStrategy(@NonNull Context context, @NonNull String phoneNumber) {
        phone = Objects.requireNonNull(phoneNumber);

        int strategy = getConfig(phoneNumber);
        SubscriptionManager subscriptionManager = QueueUtils.getSubscriptionManager(context);
        final Integer sub1 = readSubscription(subscriptionManager, 0);
        final Integer sub2 = readSubscription(subscriptionManager, 1);
        if (strategy == SIM1_SIM2) {
            subscription = GlobalUtil.firstNonNull(sub1, sub2);
            deliverable = true;
        } else if (strategy == SIM2_SIM1) {
            subscription = GlobalUtil.firstNonNull(sub2, sub1);
            deliverable = true;
        } else if (strategy == SIM1_ONLY) {
            subscription = sub1;
            deliverable = sub1 != null;
        } else if (strategy == SIM2_ONLY) {
            subscription = sub2;
            deliverable = sub2 != null;
        } else {
            subscription = null;
            deliverable = false;
        }
    }

    private Integer readSubscription(SubscriptionManager subscriptionManager, int index) {
        if (subscriptionManager != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
            try {
                @SuppressLint("MissingPermission")
                final SubscriptionInfo subInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(index);
                return subInfo != null ? subInfo.getSubscriptionId() : null;
            } catch (SecurityException e) {
                Teller.warn("permission is required to get active SIM subscriptions", e);
            } catch (RuntimeException e) {
                Teller.warn("could not get active SIM subscriptions", e);
            }

        return null;
    }

    private int getConfig(String phone) {
        AppConfig appConfig = AppConfig.getInstance();
        if (phone.startsWith("+2135"))
            return appConfig.getAsInt(StringNumberSetting.OOREDOO_SMS_CONFIG);
        else if (phone.startsWith("+2136"))
            return appConfig.getAsInt(StringNumberSetting.MOBILIS_SMS_CONFIG);
        else if (phone.startsWith("+2137"))
            return appConfig.getAsInt(StringNumberSetting.DJEZZY_SMS_CONFIG);
        else
            return appConfig.getAsInt(StringNumberSetting.OTHER_SMS_CONFIG);
    }

    @NonNull
    public String getPhone() {
        return phone;
    }

    @Nullable
    public Integer getSubscription() {
        return subscription;
    }

    public boolean isDeliverable() {
        return deliverable;
    }

    @Override
    @NonNull
    public String toString() {
        return "SimStrategy{" +
                "subscription=" + subscription +
                ", phone='" + phone + '\'' +
                ", deliverable=" + deliverable +
                '}';
    }
}
