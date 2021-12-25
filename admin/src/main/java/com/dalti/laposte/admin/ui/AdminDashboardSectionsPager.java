package com.dalti.laposte.admin.ui;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dalti.laposte.admin.R;
import com.dalti.laposte.core.ui.Named;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.material.tabs.TabLayoutMediator;

import dz.jsoftware95.queue.common.Supplier;
import dz.jsoftware95.silverbox.android.common.Check;

public final class AdminDashboardSectionsPager extends FragmentStateAdapter {

    public AdminDashboardSectionsPager(FragmentActivity activity) {
        super(activity);
    }

    @Override
    public int getItemCount() {
        return Sections.values().length;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return getFragment(position);
    }

    private Fragment getFragment(int position) {
        return Sections.values()[position].newFragment();
    }

    public static TabLayoutMediator.TabConfigurationStrategy newTabStyler() {
        return (tab, position) ->
                tab.setText(QueueUtils.getString(Sections.values()[position].title));
    }

    public enum Sections implements Named {
        PROGRESS(AdminProgressFragment::new, R.string.progress),
        PANEL(AdminPanelFragment::new, R.string.panel),
        ;
        private final Supplier<Fragment> supplier;
        private final int title;

        Sections(Supplier<Fragment> supplier, @StringRes int title) {
            this.supplier = Check.nonNull(supplier);
            this.title = title;
        }

        public Fragment newFragment() {
            return supplier.get();
        }

        public int getTitleID() {
            return title;
        }
    }
}
