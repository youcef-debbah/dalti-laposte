package com.dalti.laposte.admin.ui;

import com.dalti.laposte.core.ui.AbstractQueueService;

import org.jetbrains.annotations.NotNull;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminQueueService extends AbstractQueueService {

    @Override
    public void onNewToken(@NotNull String token) {
        super.onNewToken(token);
    }
}
