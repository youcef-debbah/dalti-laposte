package com.dalti.laposte.admin.model;

import android.graphics.Bitmap;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

import com.dalti.laposte.core.entity.Activation;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.glxn.qrgen.android.QRCode;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import dz.jsoftware95.queue.api.Pair;
import dz.jsoftware95.silverbox.android.common.Cache;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

@MainThread
public class SelectedActivation {

    private static final Cache<Pair<String, Integer>, Bitmap> cache = new Cache<>();

    private final Activation activation;
    private final Bitmap codeImage;

    public SelectedActivation(Activation activation, Integer imageSize) {
        this.activation = activation;
        this.codeImage = getCodeImage(activation != null ? activation.getCode() : null,
                imageSize != null ? imageSize : QueueUtils.DEFAULT_BACKGROUND_SIZE);
    }

    private static Bitmap getCodeImage(String code, int size) {
        int imageSize = Math.max(size, 100) - 10;
        if (code == null)
            return ContextUtils.getWhiteBackground(imageSize);
        else
            return cache.computeIfAbsent(new Pair<>(code, size), SelectedActivation::newCodeImage);
    }

    private static Bitmap newCodeImage(Pair<String, Integer> info) {
        String code = Objects.requireNonNull(info.getPrimaryValue());
        int size = Objects.requireNonNull(info.getSecondaryValue());
        return QRCode.from(code)
                .withSize(size, size)
                .withCharset(StandardCharsets.UTF_8.name())
                .withErrorCorrection(ErrorCorrectionLevel.H)
                .bitmap();
    }

    @Nullable
    public Activation getActivation() {
        return activation;
    }

    @Nullable
    public String getActivationCode() {
        return activation != null? activation.getCode() : null;
    }

    public Bitmap getCodeImage() {
        return codeImage;
    }
}
