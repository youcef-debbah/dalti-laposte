package com.dalti.laposte.client.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.ViewDataBinding;
import androidx.viewpager2.widget.ViewPager2;

import com.dalti.laposte.client.BR;
import com.dalti.laposte.client.R;
import com.dalti.laposte.core.repositories.NotificationUtils;
import com.dalti.laposte.core.repositories.SmsRepository;
import com.dalti.laposte.core.ui.AbstractDocActivity;
import com.dalti.laposte.core.ui.PageScrollLogger;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@UiThread
@AndroidEntryPoint
public final class ClientDashboardActivity extends AbstractDocActivity {

    private OnBackPressedCallback doubleBackListener;

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_client_dashboard);
        binding.setVariable(BR.activity, this);
        doubleBackListener = newDoubleBackListener();
        getOnBackPressedDispatcher().addCallback(this, doubleBackListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntentData();
        setupViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (doubleBackListener != null)
            doubleBackListener.setEnabled(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntentData();
    }

    private void handleIntentData() {
        SmsRepository.cancelByClientConfirmation(getStringExtra(NotificationUtils.KEY_NOTIFICATION_SMS_TOKEN));
    }

    private void setupViews() {
        ViewPager2 viewPager = findViewById(R.id.dashboard_pager);
        viewPager.setAdapter(new ClientDashboardSectionsPager(this));
        TabLayout tabs = findViewById(R.id.dashboard_tabs);
        new TabLayoutMediator(tabs, viewPager, ClientDashboardSectionsPager.newTabStyler()).attach();

        viewPager.registerOnPageChangeCallback(newPageScrollStateListener(tabs, findViewById(R.id.dashboard_fab)));
        viewPager.registerOnPageChangeCallback(new PageScrollLogger(tabs, ClientDashboardSectionsPager.Sections.values()));
    }

    @NotNull
    private static ViewPager2.OnPageChangeCallback newPageScrollStateListener(TabLayout tabs,
                                                                              FloatingActionButton fab) {
        return new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrollStateChanged(int state) {
                if (fab != null) {
                    if (state == 0 && tabs.getSelectedTabPosition() == ClientDashboardSectionsPager.TAB_WITH_FAB)
                        fab.show();
                    else
                        fab.hide();
                }
            }
        };
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
        // empty to not inherit the back button
    }

    public String getNamespace() {
        return "client_dashboard_activity";
    }
}