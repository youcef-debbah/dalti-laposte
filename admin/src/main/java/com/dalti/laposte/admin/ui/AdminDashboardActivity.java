package com.dalti.laposte.admin.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.ViewDataBinding;
import androidx.viewpager2.widget.ViewPager2;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.PageScrollLogger;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class AdminDashboardActivity extends AbstractQueueActivity {

    private OnBackPressedCallback doubleBackListener;

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_admin_dashboard);
        binding.setVariable(BR.activity, this);
        doubleBackListener = newDoubleBackListener();
        getOnBackPressedDispatcher().addCallback(this, doubleBackListener);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setupViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (doubleBackListener != null)
            doubleBackListener.setEnabled(true);
    }

    private void setupViews() {
        ViewPager2 viewPager = findViewById(R.id.dashboard_pager);
        viewPager.setAdapter(new AdminDashboardSectionsPager(this));
        TabLayout tabs = findViewById(R.id.dashboard_tabs);
        new TabLayoutMediator(tabs, viewPager, AdminDashboardSectionsPager.newTabStyler()).attach();

        viewPager.registerOnPageChangeCallback(new PageScrollLogger(tabs, AdminDashboardSectionsPager.Sections.values()));

        Toolbar toolbar = findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
    }
}
