package com.dalti.laposte.core;

import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import dz.jsoftware95.silverbox.android.backend.LiveDataWrapper;
import dz.jsoftware95.silverbox.android.concurrent.SystemWorker;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest implements Runnable {

    private LiveDataWrapper<String> liveDataWrapper = new LiveDataWrapper<>();

    @Test
    public void useAppContext() {
        SystemWorker.MAIN.execute(this);
    }

    @Override
    public void run() {
        assertThat(liveDataWrapper.getLiveData().getValue(), nullValue());
        liveDataWrapper.getLiveData().observeForever(System.out::println);
        MutableLiveData<String> newSource = new MutableLiveData<>();
        liveDataWrapper.setSource(newSource);
        newSource.setValue("value");
        assertThat(liveDataWrapper.getLiveData().getValue(), is("value"));
        liveDataWrapper.setSource(null);
        assertThat(liveDataWrapper.getLiveData().getValue(), nullValue());
    }
}