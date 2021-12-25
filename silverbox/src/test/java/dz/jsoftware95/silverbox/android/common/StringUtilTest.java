package dz.jsoftware95.silverbox.android.common;

import org.junit.Test;

import dz.jsoftware95.queue.common.IdentityManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

public class StringUtilTest {

    @Test
    public void testParseProgressID() {
        assertThat(StringUtil.parseProgressesIDs(2, "1:2"), containsInAnyOrder(2L, 1000002L, 2000002L));
        assertThat(StringUtil.parseProgressesIDs(3, ""), containsInAnyOrder(3L));
        assertThat(StringUtil.parseProgressesIDs(4, null), containsInAnyOrder(4L));
        assertThat(StringUtil.parseProgressesIDs(5, "20s55:574"), containsInAnyOrder(5L, 574000005L));
    }

    @Test
    public void testIdentityManager() {
        assertThat(IdentityManager.getProgressRank(3000006), equalTo(3));
    }

    @Test
    public void parse() {
        assertThat(StringUtil.parse(null), empty());
        assertThat(StringUtil.parse(""), contains(""));
        assertThat(StringUtil.parse("a:b"), contains("a", "b"));
        assertThat(StringUtil.parse("a:b:x*y"), contains("a", "b", "x*y"));
    }

    @Test
    public void trimStart() {
        System.out.println(StringUtil.trimPrefix("start0123", "start"));
    }
}