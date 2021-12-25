package com.dalti.laposte.admin.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminTestActivity extends AppCompatActivity {

    MutableLiveData<String> output = new MutableLiveData<>(null);

    @Override
    @SuppressLint("SetTextI18n")
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewDataBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_admin_test);
        binding.setVariable(BR.activity, this);


    }

    public LiveData<String> getOutput() {
        return output;
    }
}
