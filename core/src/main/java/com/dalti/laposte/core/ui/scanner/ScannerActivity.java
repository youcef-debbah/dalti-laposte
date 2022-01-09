package com.dalti.laposte.core.ui.scanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraFilter;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.BooleanSetting;
import com.dalti.laposte.core.repositories.Event;
import com.dalti.laposte.core.repositories.InputProperty;
import com.dalti.laposte.core.repositories.IntegerSetting;
import com.dalti.laposte.core.repositories.StringNumberSetting;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.concurrent.SystemWorker;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
@SuppressLint("UnsafeOptInUsageError")
public class ScannerActivity extends AbstractQueueActivity implements ActivationCodeListener {

    private final CameraSelector backCameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

    @Nullable
    private PreviewView codePreview;
    @Nullable
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Camera camera;

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        BindingUtil.setContentView(this, R.layout.activity_scanner);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        codePreview = findViewById(R.id.code_preview);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startCamera();
    }

    private void startCamera() {
        if (codePreview != null && cameraProviderFuture != null) {
            cameraProviderFuture.addListener(() -> {
                try {
                    bindCameraPreview(cameraProviderFuture.get(), codePreview);
                    Teller.log(Event.StartCamera.NAME);
                } catch (Exception e) {
                    Teller.warn("could not get camera feed to scan activation code", e);
                    QueueUtils.showToast(this, R.string.no_device_support);
                    finish();
                }
            }, SystemWorker.MAIN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCameraConfig();
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider, @NonNull PreviewView codePreview) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(codePreview.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(SystemWorker.MAIN, new QRCodeImageAnalyzer(this));

        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(this, getDefaultCameraSelector(), preview, imageAnalysis);
        updateCameraConfig();
    }

    private void updateCameraConfig() {
        if (camera != null)
            camera.getCameraControl().enableTorch(AppConfig.getInstance().get(BooleanSetting.SCANNER_FLASH_LIGHT));
    }

    private CameraSelector getDefaultCameraSelector() {
        int cameraIndex = AppConfig.getInstance().getAsInt(StringNumberSetting.SCANNING_CAMERA_INDEX);
        if (cameraIndex > 0)
            return new CameraSelector.Builder()
                    .addCameraFilter(new IndexCameraFilter(cameraIndex))
                    .build();
        else
            return backCameraSelector;
    }

    @Override
    public boolean onCodeFound(String code) {
        Assert.isMainThread();
        QueueUtils.vibrate(AppConfig.getInstance().getInt(IntegerSetting.QR_SCANNER_VIBRATION_DURATION));
        Intent data = new Intent();
        data.putExtra(InputProperty.ACTIVATION_CODE.name(), code);
        setResult(Activity.RESULT_OK, data);
        finish();
        Teller.log(Event.ScanQR.NAME);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        codePreview = null;
        cameraProviderFuture = null;
        camera = null;
    }

    private static final class IndexCameraFilter implements CameraFilter {
        private final int index;

        public IndexCameraFilter(int index) {
            this.index = index;
        }

        @NonNull
        @Override
        public List<CameraInfo> filter(@NonNull List<CameraInfo> cameraInfos) {
            if (index < 0 || index >= cameraInfos.size())
                return cameraInfos;
            else
                return Collections.singletonList(cameraInfos.get(index));
        }
    }
}
