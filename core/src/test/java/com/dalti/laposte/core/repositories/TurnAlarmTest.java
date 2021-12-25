package com.dalti.laposte.core.repositories;

import org.junit.Test;

public class TurnAlarmTest {

    @Test
    public void name() {
        System.out.println(NotificationUtils.getNotificationID(Long.MAX_VALUE - 1));
    }
}