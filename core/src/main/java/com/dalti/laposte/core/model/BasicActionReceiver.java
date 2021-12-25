package com.dalti.laposte.core.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class BasicActionReceiver extends BroadcastReceiver {

    public static final String ACTION_KEY = "ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
    }
}
