package com.dalti.laposte.client.ui;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import com.dalti.laposte.client.R;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BasicActivity;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class ClientSettingsActivity extends BasicActivity {

    @Override
    protected void onCreate(Bundle savedState) {
        setTheme(R.style.Theme_App_Settings);
        super.onCreate(savedState);
        BindingUtil.setContentView(this, R.layout.activity_client_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
