package com.dalti.laposte.core.ui;

import androidx.viewpager2.widget.ViewPager2;

import com.dalti.laposte.core.repositories.Teller;
import com.google.android.material.tabs.TabLayout;

import dz.jsoftware95.queue.common.GlobalUtil;

public class PageScrollLogger extends ViewPager2.OnPageChangeCallback {

    private final TabLayout tabs;
    private final Named[] screenNames;

    public PageScrollLogger(TabLayout tabs, Named[] screenNames) {
        this.tabs = tabs;
        this.screenNames = screenNames;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == 0 && tabs != null) {
            Named namedTab = GlobalUtil.getElement(tabs.getSelectedTabPosition(), screenNames);
            if (namedTab != null)
                Teller.logScreenViewEvent(tabs.getContext(), namedTab.name());
        }
    }
}
