/*
 * Copyright (c) 2018 Youcef DEBBAH (youcef-debbah@hotmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the Software) to deal in the Software without restriction
 * but under the following conditions:
 *
 * - This notice shall be included in all copies and portions of the Software.
 * - The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND (Implicit or Explicit).
 *
 */

package dz.jsoftware95.silverbox.android.middleware;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;

import dz.jsoftware95.silverbox.android.frontend.ForegroundService;

public abstract class StatefulActivity extends AppCompatActivity implements
        StateOwner,
        LifecycleOwner,
        StatefulContext {

    protected final String TAG = getClass().getSimpleName();

    @NonNull
    public abstract <T extends StatefulModel> T getViewModel(Class<T> viewModelClass);

    protected abstract <T extends MutableStateOwner> T linkState(@NonNull T stateOwner);

    protected abstract <T extends MutableStateOwner> T unlinkState(@NonNull T stateOwner);

    protected abstract void saveState(@NonNull Bundle state);

    protected abstract void loadState(@Nullable Bundle state);

    protected abstract void setBinding(@Nullable ViewDataBinding binding);

    public abstract boolean startServiceThenUpgradeToForeground(Class<? extends ForegroundService> serviceClass);

    public abstract boolean bindServiceThenUpgradeToForeground(Intent intent);

    public String getStringExtra(String key) {
        Intent intent = getIntent();
        return intent != null ? intent.getStringExtra(key) : null;
    }

    public int getIntegerExtra(String key, int defaultValue) {
        Intent intent = getIntent();
        return intent != null ? intent.getIntExtra(key, defaultValue) : defaultValue;
    }

    public int getIntegerState(Bundle bundle, String key, int defaultValue) {
        if (bundle != null && bundle.containsKey(key))
            return bundle.getInt(key, defaultValue);
        else
            return getIntegerExtra(key, defaultValue);
    }

    public long getLongExtra(String key, long defaultValue) {
        Intent intent = getIntent();
        return intent != null ? intent.getLongExtra(key, defaultValue) : defaultValue;
    }

    public long getLongState(Bundle bundle, String key, long defaultValue) {
        if (bundle != null && bundle.containsKey(key))
            return bundle.getLong(key, defaultValue);
        else
            return getLongExtra(key, defaultValue);
    }

    public boolean getBooleanExtra(String key, boolean defaultValue) {
        Intent intent = getIntent();
        return intent != null ? intent.getBooleanExtra(key, defaultValue) : defaultValue;
    }

    public void startActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    public void openActivity(Class<? extends Activity> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void cancelResultAndFinish() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
