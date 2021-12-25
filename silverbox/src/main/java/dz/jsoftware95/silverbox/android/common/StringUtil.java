package dz.jsoftware95.silverbox.android.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.IdentityManager;

public final class StringUtil {

    private static final String TAG = "StringUtil";

    public static final String NEW_LINE = System.getProperty("line.separator");
    public static final String NULL = "null";
    public static final String FALSE = "false";
    public static final char KEY_VALUE_SEPARATOR = '=';
    public static final char DATA_SEPARATOR = ';';

    private StringUtil() {
        throw new IllegalAccessError();
    }

    public static Integer parseAsInteger(@Nullable Object value) {
        if (value instanceof Number)
            return ((Number) value).intValue();
        else
            return parseInteger(toString(value));
    }

    public static Long parseAsLong(@Nullable Object value) {
        if (value instanceof Number)
            return ((Number) value).longValue();
        else
            return parseLong(toString(value));
    }

    public static Long parseLong(@Nullable String value) {
        if (isBlank(value))
            return null;
        else
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                Log.w(TAG, "could not parse input as long: " + value, e);
                return null;
            }
    }

    public static Double parseDouble(@Nullable String value) {
        if (isBlank(value))
            return null;
        else
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                Log.w(TAG, "could not parse input as double: " + value, e);
                return null;
            }
    }

    public static Integer parsePositiveInteger(@Nullable String value) {
        Integer integer = parseInteger(value);
        return (integer == null || integer < 0) ? null : integer;
    }

    public static Integer parseInteger(@Nullable String value) {
        if (isBlank(value))
            return null;
        else
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
    }

    public static int parseInt(@Nullable String value, int defaultValue) {
        Integer integer = parseInteger(value);
        return integer == null ? defaultValue : integer;
    }

    public static long parseLong(@Nullable String input, long defaultValue) {
        Long value = parseLong(input);
        return value == null ? defaultValue : value;
    }

    public static double parseDouble(@Nullable String input, double defaultValue) {
        Double value = parseDouble(input);
        return value == null ? defaultValue : value;
    }

    @Contract(value = "null -> true", pure = true)
    public static boolean isZero(@Nullable Integer value) {
        return value == null || value == 0;
    }

    @Contract(value = "null -> true", pure = true)
    public static boolean isBlank(@Nullable String value) {
        return isNullOrEmpty(value) || NULL.equalsIgnoreCase(value);
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean notBlank(@Nullable String value) {
        return !isBlank(value);
    }

    public static String toString(Object object) {
        return object == null ? null : String.valueOf(object);
    }

    public static String toNonNullString(Object object) {
        return object == null ? "" : String.valueOf(object);
    }

    public static String toString(Object object, String nullValue) {
        return object == null ? nullValue : String.valueOf(object);
    }

    public static boolean isNullOrEmpty(CharSequence value) {
        return value == null || isNullOrEmpty(value.toString());
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String extractString(@Nullable TextView view) {
        return view != null ? toString(view.getText()) : null;
    }

    public static Set<Long> parseProgressesIDs(long service, String value) {
        if (isBlank(value))
            return Sets.newHashSet(IdentityManager.getProgressID(service, 0));
        else {
            String[] rowRanks = value.split(GlobalConf.SEPARATOR, GlobalConf.MAX_PROGRESSES_PER_SERVICE);
            Set<Long> ids = Sets.newHashSetWithExpectedSize(rowRanks.length);
            ids.add(IdentityManager.getProgressID(service, 0));
            for (String rowRank : rowRanks) {
                Integer rank = parseInteger(rowRank);
                if (rank != null)
                    ids.add(IdentityManager.getProgressID(service, rank));
            }
            return ids;
        }
    }

    public static LinkedList<String> parse(String text) {
        if (text == null)
            return new LinkedList<>();
        else {
            LinkedList<String> result = new LinkedList<>();
            Collections.addAll(result, text.split(GlobalConf.SEPARATOR));
            return result;
        }
    }

    public static int cast(long value) {
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
            throw new RuntimeException("number too big to be casted as int: " + value);
        else
            return (int) value;
    }

    @Nullable
    public static String getString(Map<String, String> data, String key, String defaultValue) {
        if (data == null || key == null)
            return defaultValue;
        else {
            String value = data.get(key);
            return isBlank(value) ? defaultValue : value;
        }
    }

    @Nullable
    public static String getString(Map<String, String> data, String key) {
        return getString(data, key, null);
    }

    public static String getString(Intent data, String key) {
        if (data == null || key == null)
            return null;
        else {
            String value = data.getStringExtra(key);
            return isBlank(value) ? null : value;
        }
    }

    public static String getString(TextView input) {
        return input == null ? null : toString(input.getText());
    }

    public static Boolean parseBoolean(Map<String, String> data, String key) {
        String value = getString(data, key);
        return isBlank(value) ? null : Boolean.parseBoolean(value);
    }

    public static boolean isTrue(Map<String, String> data, String key) {
        Boolean value = parseBoolean(data, key);
        return value != null && value;
    }

    public static boolean isFalse(Map<String, String> data, String key) {
        Boolean value = parseBoolean(data, key);
        return value != null && !value;
    }

    public static boolean isTrue(Object value) {
        if (value == null)
            return false;
        else if (value instanceof Boolean)
            return (Boolean) value;
        else {
            String string = value.toString();

            if (isBlank(string) || "false".equalsIgnoreCase(string))
                return false;

            if ("true".equalsIgnoreCase(string))
                return true;

            Long number = StringUtil.parseAsLong(value);
            return number != null && number != 0;
        }
    }

    public static int length(String msg) {
        if (isBlank(msg))
            return 0;
        else
            return msg.length();
    }

    public static String getString(int index, String[] options) {
        if (options == null || index >= options.length || index < 0)
            return null;
        else
            return options[index];
    }

    public static int reversedCompare(int int1, int int2) {
        return -Integer.compare(int1, int2);
    }

    @Nullable
    public static Integer getInt(@Nullable Integer index, int[] values) {
        if (index != null && index < values.length && index > -1)
            return values[index];
        else
            return null;
    }

    public static String androidVersion(Integer api) {
        if (api == null)
            return null;
        else {
            if (api < 21)
                return "older than 5.0";
            else if (api == 21)
                return "5.0";
            else if (api == 22)
                return "5.1";
            else if (api == 23)
                return "6.0";
            else if (api == 24)
                return "7.0";
            else if (api == 25)
                return "7.1";
            else if (api == 26)
                return "8.0.0";
            else if (api == 27)
                return "8.1.0";
            else if (api == 28)
                return "9";
            else if (api == 29)
                return "10";
            else if (api == 30)
                return "11";
            else
                return "newer than 11.0";
        }
    }

    public static int getValue(LiveData<Integer> data) {
        if (data != null) {
            Integer value = data.getValue();
            if (value != null)
                return value;
        }
        return 0;
    }

    public static int hash(long value) {
        return (int) (value ^ (value >>> 32));
    }

    public static String trimPrefix(String value, String prefix) {
        if (isNullOrEmpty(prefix) || isNullOrEmpty(value))
            return value;
        else
            return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
    }

    public static Map<String, String> toStringMap(Bundle bundle) {
        if (bundle == null || bundle.isEmpty())
            return new HashMap<>(1);
        else {
            HashMap<String, String> result = new HashMap<>(bundle.size());
            for (String key : bundle.keySet())
                result.put(key, toString(bundle.get(key)));
            return result;
        }
    }

    public static Map<String, String> toStringMap(Map<?, ?> data) {
        if (data == null)
            return new HashMap<>(1);
        else {
            HashMap<String, String> result = new HashMap<>(data.size());
            for (Map.Entry<?, ?> entry : data.entrySet())
                result.put(toString(entry.getKey()), toString(entry.getValue()));
            return result;
        }
    }

    public static String toDataString(Bundle bundle) {
        if (bundle == null || bundle.isEmpty())
            return null;
        else {
            Set<String> keys = bundle.keySet();
            StringBuilder data = new StringBuilder(keys.size() * 32);
            for (String key : keys)
                data.append(key).append(KEY_VALUE_SEPARATOR).append(toString(bundle.get(key))).append(DATA_SEPARATOR);
            return data.toString();
        }
    }

    public static String appendData(String key, String data) {
        return key + KEY_VALUE_SEPARATOR + data + DATA_SEPARATOR;
    }

    @NotNull
    public static String toString(String event, Bundle params) {
        return event + " with data: " + toDataString(params);
    }

    public static String fill(String token, int size) {
        if (isNullOrEmpty(token))
            return token;
        else {
            final int count = Math.max(size, 0);
            final StringBuilder result = new StringBuilder(token.length() * count);
            for (int i = 0; i < count; i++)
                result.append(token);
            return result.toString();
        }
    }
}
