package dz.jsoftware95.silverbox.android.middleware;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    public static final int ONE_SECOND_MILLIS = 1000;
    public static final int ONE_MINUTE_MILLIS = ONE_SECOND_MILLIS * 60;
    public static final int ONE_HOUR_MILLIS = ONE_MINUTE_MILLIS * 60;
    public static final int ONE_DAY_MILLIS = ONE_HOUR_MILLIS * 24;

    public static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
    public static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.MEDIUM);
    public static final DateFormat EXACT_TIME_FORMAT = new SimpleDateFormat("hh:mm:ss.SS a", Locale.UK);
    public static final DateFormat SHORT_TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT);
    public static final DateFormat DATE_TIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    public static String formatAsDate(long epoch) {
        return DATE_FORMAT.format(new Date(epoch));
    }

    @Nullable
    public static String formatAsDateTime(Long epoch) {
        if (epoch == null || epoch < 1)
            return null;
        else
            return DATE_TIME_FORMAT.format(new Date(epoch));
    }

    @Nullable
    public static String formatAsTime(Long epoch) {
        if (epoch == null || epoch < 1)
            return null;
        else
            return TIME_FORMAT.format(new Date(epoch));
    }

    @Nullable
    public static String formatAsShortTime(Long epoch) {
        if (epoch == null || epoch < 1)
            return null;
        else
            return SHORT_TIME_FORMAT.format(new Date(epoch));
    }

    @Nullable
    public static Long getDays(Long epoch) {
        if (epoch == null || epoch < 1)
            return null;
        else
            return epoch / ONE_DAY_MILLIS;
    }

    public static String formatAsTimeExact(Long epoch) {
        if (epoch == null || epoch < 1)
            return null;
        else
            return EXACT_TIME_FORMAT.format(new Date(epoch));
    }
}
