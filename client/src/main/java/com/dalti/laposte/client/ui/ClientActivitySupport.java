package com.dalti.laposte.client.ui;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.dalti.laposte.client.R;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.QueueActivitySupport;

import javax.inject.Inject;

import dz.jsoftware95.silverbox.android.middleware.StatefulActivity;

public class ClientActivitySupport extends QueueActivitySupport {

    private static final String SOURCE = "activate_menu_item";

    @Inject
    public ClientActivitySupport() {
    }

    @Override
    public boolean inflate(AbstractQueueActivity activity, Menu menu) {
        activity.getMenuInflater().inflate(R.menu.client_menu, menu);
        return super.inflate(activity, menu);
    }

    @Override
    public boolean handleItemSelected(AbstractQueueActivity activity, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_activate) {
            Teller.logClick("item_activate");
            Intent intent = new Intent(activity, ActivationCodeFormActivity.class);
            intent.putExtra(Teller.ACTIVATION_SOURCE, SOURCE);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            return true;
        } else
            return super.handleItemSelected(activity, item);
    }

    @Override
    public Class<? extends StatefulActivity> getSettingsActivity() {
        return ClientSettingsActivity.class;
    }

    @Override
    public Class<? extends StatefulActivity> getMainActivity() {
        return ClientDashboardActivity.class;
    }
}
