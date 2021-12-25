package com.dalti.laposte.client;

import android.os.Environment;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.dalti.laposte.client.ui.ClientDashboardActivity;
import com.dalti.laposte.core.repositories.Service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import dz.jsoftware95.silverbox.android.common.LDT;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SwipeLeftAndRightTest {
    protected final String TAG = getClass().getSimpleName();
    public static final String DUMP_FILE_SUFFIX = ".dump";
    public static final File MEMORY_DUMP_DIR = new File(Environment.getExternalStorageDirectory(), "dumps/swipeLeftAndRight");
    public static final String DUMP_PATH_FOR_SWIPE_LEFT_AND_RIGHT_INIT = new File(MEMORY_DUMP_DIR, "SwipeLeftAndRight-0" + DUMP_FILE_SUFFIX).getAbsolutePath();
    private static final int STEPS_COUNT = 50;
    private static final int SWIPES_COUNT_PER_STEP = 50;
    private static final String BOTTOM_SERVICE_NAME = "service-" + Service.PAGE_SIZE;

    static {
        if (!MEMORY_DUMP_DIR.exists())
            MEMORY_DUMP_DIR.mkdirs();
        else
            for (File file : MEMORY_DUMP_DIR.listFiles())
                file.delete();
    }

    @Rule
    public ActivityScenarioRule<ClientDashboardActivity> activityRule = new ActivityScenarioRule<>(ClientDashboardActivity.class);

    @Test
    public void keepSwipeLeftAndRight() {
        final ViewInteraction pager = onView(withId(R.id.dashboard_pager));

        swipeLeftAndRight(pager);
        dumpData(DUMP_PATH_FOR_SWIPE_LEFT_AND_RIGHT_INIT);

        //        for (int step = 1; step <= STEPS_COUNT; step++) {
        //            for (int i = 1; i <= SWIPES_COUNT_PER_STEP; i++) {
        //                swipeLeftAndRight(pager);
        //                Log.i(TAG, "keepSwipeLeftAndRight: " + i + '/' + SWIPES_COUNT_PER_STEP);
        //            }
        //            dumpData(new File(MEMORY_DUMP_DIR, "SwipeLeftAndRight-" + String.format("%03d", step) + DUMP_FILE_SUFFIX).getAbsolutePath());
        //        }

        AppWorker.DATABASE.interrupt();
        LDT.sleepForever();
    }

    private void swipeLeftAndRight(ViewInteraction pager) {
        pager.perform(swipeUp());
        pager.perform(swipeLeft()).perform(swipeLeft());
        pager.perform(swipeRight()).perform(swipeRight());
    }

    private void dumpData(String filename) {
        //        Log.i(TAG, "dumping data to: " + filename);
        //        try {
        //            System.gc();
        //            LDT.sleep(1);
        //            Debug.dumpHprofData(filename);
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }
    }
}