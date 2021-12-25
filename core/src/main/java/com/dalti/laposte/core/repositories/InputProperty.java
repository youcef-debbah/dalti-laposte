package com.dalti.laposte.core.repositories;

import java.util.regex.Pattern;

import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.silverbox.android.common.StringUtil;

public enum InputProperty implements Property {
    PRINCIPAL_NAME(Property.ID_1, GlobalConf.MIN_NAME_LENGTH, GlobalConf.MAX_NAME_LENGTH, GlobalConf.NAME_PATTERN),
    PRINCIPAL_PASSWORD(Property.ID_3, GlobalConf.MIN_PASSWORD_LENGTH, GlobalConf.MAX_PASSWORD_LENGTH, GlobalConf.PASSWORD_PATTERN),

    ACTIVATION_CODE(Property.ID_8, GlobalConf.ACTIVATION_CODE_LENGTH),
    ;
    private final long key;
    private final int minLength;
    private final int maxLength;
    private final Pattern regex;

    InputProperty(int key) {
        this(key, 0, Integer.MAX_VALUE, null);
    }

    InputProperty(int key, Integer length) {
        this(key, length, length, null);
    }

    InputProperty(int key, Integer minLength, Integer maxLength) {
        this(key, minLength, maxLength, null);
    }

    InputProperty(int key, int minLength, int maxLength, Pattern regex) {
        this.key = key;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.regex = regex;
    }

    @Override
    public long key() {
        return key;
    }

    public boolean isTooLong(String value) {
        return isNotNull(value) && value.length() > maxLength;
    }

    public boolean isTooShort(String value) {
        return isNotNull(value) && value.length() < minLength;
    }

    public boolean isValid(String value) {
        return isNotNull(value) && isNonNullValid(value);
    }

    public static Validity validatePhoneInput(String value) {
        return validate(value, GlobalConf.PHONE_INPUT_PATTERN, GlobalConf.PHONE_INPUT_LENGTH);
    }

    public static Validity validate(String value, Pattern regex, int length) {
        return validate(value, regex, length, length);
    }

    public static Validity validate(String value, Pattern regex, int minLength, int maxLength) {
        if (StringUtil.isNullOrEmpty(value))
            return Validity.NULL;
        else {
            int length = value.length();
            if (length < minLength)
                return Validity.TOO_SHORT;
            else if (length > maxLength)
                return Validity.TOO_LONG;

            if ((regex != null && !regex.matcher(value).matches()))
                return Validity.INVALID;
            else
                return Validity.VALID;
        }
    }

    private boolean isNonNullValid(String value) {
        int length = value.length();
        boolean bounded = length <= maxLength && length >= minLength;
        return regex != null ? bounded && regex.matcher(value).matches() : bounded;
    }
}
