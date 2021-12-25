package dz.jsoftware95.silverbox.android.common;

import android.view.View;

import java.io.Serializable;

public interface InputListener<T, C> extends Serializable {

    void onInput(C context, View view, T input);
}
