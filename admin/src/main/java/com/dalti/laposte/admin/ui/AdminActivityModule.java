package com.dalti.laposte.admin.ui;

import android.app.Activity;
import android.content.ClipboardManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;

@Module
@InstallIn(ActivityComponent.class)
public interface AdminActivityModule {

    @Provides
    static ClipboardManager provideClipboardManager(Activity activity) {
        return (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
    }
}
