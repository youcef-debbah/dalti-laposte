package com.dalti.laposte.client.ui;

import com.dalti.laposte.core.ui.AbstractQueueService;

import org.jetbrains.annotations.NotNull;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientQueueService extends AbstractQueueService {

    @Override
    public void onNewToken(@NotNull String token) {
        super.onNewToken(token);
    }
}