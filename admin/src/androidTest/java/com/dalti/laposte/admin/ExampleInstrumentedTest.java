package com.dalti.laposte.admin;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static final String TAG = "ExampleInstrumentedTest";

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.dalti.laposte.queue.android.admin", appContext.getPackageName());
    }

    @Test
    public void testLiveDataSwitch() {
        MutableLiveData<Data> liveData = new MutableLiveData<>();
        LiveData<String> liveYdata = Transformations.map(liveData, Data::getY);
        Log.i(TAG, "init value: " + liveYdata.getValue());
        liveData.postValue(new Data(2));
        Log.i(TAG, "data 2 vakye:" + liveData.getValue());
        liveData.postValue(null);
        Log.i(TAG, "null data: " + liveData.getValue());
    }

    private static final class Data {

        int x;

        public Data(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public String getY() {
            return "y=" + x * 2;
        }
    }
}