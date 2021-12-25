package dz.jsoftware95.queue.android.silverbox;

import org.junit.Test;

import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void sandbox() {
        System.out.println(TimeUtils.formatAsTimeExact(System.currentTimeMillis()));
    }
}