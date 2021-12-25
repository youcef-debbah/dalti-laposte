package com.dalti.laposte.admin.ui;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.dalti.laposte.admin.R;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.QueueActivitySupport;

import javax.inject.Inject;

import dz.jsoftware95.silverbox.android.middleware.StatefulActivity;

public class AdminActivitySupport extends QueueActivitySupport {

    private static final String SOURCE = "profile_menu_item";

    @Inject
    public AdminActivitySupport() {
    }

    @Override
    public boolean inflate(AbstractQueueActivity activity, Menu menu) {
        activity.getMenuInflater().inflate(R.menu.admin_menu, menu);
        return super.inflate(activity, menu);
    }

    @Override
    public boolean handleItemSelected(AbstractQueueActivity activity, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_profile) {
            Teller.logClick("item_profile");
            Intent intent = new Intent(activity, ProfileActivity.class);
            intent.putExtra(Teller.ACTIVATION_SOURCE, SOURCE);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            return true;
        } else
            return super.handleItemSelected(activity, item);
    }

    @Override
    public Class<? extends StatefulActivity> getSettingsActivity() {
        return AdminSettingsActivity.class;
    }

    @Override
    public Class<? extends StatefulActivity> getMainActivity() {
        return AdminDashboardActivity.class;
    }
}
