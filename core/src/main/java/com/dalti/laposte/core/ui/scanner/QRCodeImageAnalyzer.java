package com.dalti.laposte.core.ui.scanner;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import static android.graphics.ImageFormat.YUV_420_888;
import static android.graphics.ImageFormat.YUV_422_888;
import static android.graphics.ImageFormat.YUV_444_888;

public class QRCodeImageAnalyzer implements ImageAnalysis.Analyzer {
    private final WeakReference<ActivationCodeListener> listener;

    public QRCodeImageAnalyzer(ActivationCodeListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        try {
            ActivationCodeListener activationCodeListener = listener.get();
            if (activationCodeListener != null)
                decodeActivationCode(imageProxy, activationCodeListener);
        } finally {
            imageProxy.close();
        }
    }

    public void decodeActivationCode(@NonNull ImageProxy image, ActivationCodeListener activationCodeListener) {
        if (image.getFormat() == YUV_420_888 || image.getFormat() == YUV_422_888 || image.getFormat() == YUV_444_888) {
            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            byte[] imageData = new byte[byteBuffer.capacity()];
            byteBuffer.get(imageData);

            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                    imageData,
                    image.getWidth(), image.getHeight(),
                    0, 0,
                    image.getWidth(), image.getHeight(),
                    false
            );

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                Result result = new QRCodeMultiReader().decode(binaryBitmap);
                if (activationCodeListener.onCodeFound(result.getText()))
                    close();
            } catch (FormatException | ChecksumException | NotFoundException e) {
                activationCodeListener.onCodeNotFound();
            }
        }
    }

    private void close() {
        listener.clear();
    }
}
