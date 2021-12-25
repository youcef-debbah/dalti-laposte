package com.dalti.laposte.core.ui;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.Teller;

import java.util.Objects;

import dz.jsoftware95.silverbox.android.middleware.StatefulActivity;

public abstract class QueueActivitySupport {

    public boolean inflate(AbstractQueueActivity activity, Menu menu) {
        Objects.requireNonNull(menu);
        MenuInflater menuInflater = activity.getMenuInflater();
        menuInflater.inflate(R.menu.core_menu, menu);
        if (activity instanceof Form)
            menuInflater.inflate(R.menu.form_menu, menu);
        return true;
    }

    public boolean handleItemSelected(AbstractQueueActivity activity, MenuItem item) {
        Objects.requireNonNull(activity);
        int id = item.getItemId();
        if (id == R.id.item_settings) {
            Teller.logClick("menu_settings");
            activity.startActivity(new Intent(activity, getSettingsActivity()));
            return true;
        } else if (id == R.id.item_help) {
            Teller.logClick("menu_help");
            activity.openActivity(AbstractQueueApplication.requireInstance().getHelpActivity());
        } else if (id == R.id.item_privacy_policy) {
            Teller.logClick("menu_privacy_policy");
            activity.openActivity(AbstractQueueApplication.requireInstance().getPrivacyPolicyActivity());
        } else if (id == R.id.item_about_us) {
            Teller.logClick("menu_about_us");
            activity.openActivity(AbstractQueueApplication.requireInstance().getAboutUsActivity());
        } else if (id == R.id.item_save && activity instanceof Form) {
            Teller.logClick("menu_save", null);
            ((Form) activity).submit();
            return true;
        }
        return false;
    }

    public abstract Class<? extends StatefulActivity> getSettingsActivity();

    public abstract Class<? extends StatefulActivity> getMainActivity();
}
