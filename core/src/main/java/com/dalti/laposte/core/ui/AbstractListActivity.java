package com.dalti.laposte.core.ui;

import android.view.Menu;
import android.view.MenuItem;

import com.dalti.laposte.R;

public abstract class AbstractListActivity extends AbstractQueueActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_refresh) {
            onRefresh();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    protected abstract void onRefresh();
}
