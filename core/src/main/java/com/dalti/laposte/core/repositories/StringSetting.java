package com.dalti.laposte.core.repositories;

import com.dalti.laposte.core.entity.ShortMessage;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;

import dz.jsoftware95.queue.common.GlobalConf;

public enum StringSetting implements StringPreference {
    REMOTE_CONFIG_VERSION("0.9.0"),
    AVERAGE_REMAINING_TIME_EXPRESSION("a:r:a*r"),

    SERVICES_API_URL(BuildConfiguration.DEFAULT_SERVICES_API_URL),

    //    AVERAGE_REMAINING_TIME_EXPRESSION("120*1000+3600*1000"),
    //    MIN_REMAINING_TIME_EXPRESSION("p=0.2:s:t:r:a:s*min(p*t,r)+a*(r-min(p*t,r))"),

    NOTE_ENG(""),
    NOTE_FRE(""),
    NOTE_ARB(""),
    TEST_PHONE_NUMBER(""),
    SMS_LATEST_OUTCOME(SmsRepository.getOutcome(ShortMessage.NULL_STATE)),
    LATEST_TURN_ALARM_PHONE(""),
    CONTACT_PHONE(""),

    UNRECORDED_EXCEPTIONS(QueueUtils.JAVA_NET_SOCKET_EXCEPTION +
            GlobalConf.SEPARATOR + QueueUtils.JAVA_NET_SOCKET_TIMEOUT_EXCEPTION +
            GlobalConf.SEPARATOR + QueueUtils.JAVA_NET_UNKNOWN_HOST_EXCEPTION
    ),

    LAST_RECEIVED_ALARM("none"),

    WEBSITE(BuildConfiguration.DEFAULT_PRODUCTION_URL_SCHEMA + BuildConfiguration.DEFAULT_PRODUCTION_HOST_NAME),
    EMAIL("contact@dalti-laposte.com"),

    CHEAPEST_ACTIVATION_PRICE_DZD("100"),
    CHEAPEST_ACTIVATION_DURATION_DAYS("15"),

    USER_RATING(""),

    ACTIVE_USERNAME(""),

    LAST_ACTIVATION_APP_CHECK(""),

    ADMIN_DOWNLOAD_LINK("https://drive.google.com/drive/folders/1HVLu5olnvN1_FieeLvlqCuv2M0iOBbdR?usp=sharing"),
    ;

    private final String defaultString;

    StringSetting(String defaultString) {
        this.defaultString = defaultString;
    }

    @Override
    public String getDefaultString() {
        return defaultString;
    }
}
