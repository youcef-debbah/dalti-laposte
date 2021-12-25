package com.dalti.laposte.core.ui;

import android.content.Context;

import dz.jsoftware95.silverbox.android.middleware.BasicFragment;

public class AbstractQueueFragment extends BasicFragment {

    private BasicHandler basicHandler;

    public BasicHandler getBasicHandler() {
        return basicHandler;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.basicHandler = new BasicHandler(getStatefulActivity());
    }
}
