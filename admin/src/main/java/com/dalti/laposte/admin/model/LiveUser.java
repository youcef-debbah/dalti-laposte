package com.dalti.laposte.admin.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.dalti.laposte.core.repositories.InputProperty;

import java.util.regex.Pattern;

import dz.jsoftware95.silverbox.android.common.StringUtil;

public class LiveUser extends MediatorLiveData<String> {

    public static final Pattern ILLEGAL_CHAR_PATTERN = Pattern.compile("\\W+");

    private final LiveData<String> phoneData;
    private final LiveData<String> nameData;

    public LiveUser(LiveData<String> phoneData, LiveData<String> nameData) {
        this.phoneData = phoneData;
        this.nameData = nameData;
        addSource(phoneData, this::onPhoneData);
        addSource(nameData, this::onNameData);
    }

    protected void onPhoneData(String phone) {
        onData(phone, nameData.getValue());
    }

    protected void onNameData(String name) {
        onData(phoneData.getValue(), name);
    }

    private void onData(String phone, String name) {
        if (StringUtil.isNullOrEmpty(phone) || InputProperty.PRINCIPAL_NAME.isNull(name))
            setValue(null);
        else
            setValue(ILLEGAL_CHAR_PATTERN.matcher(name).replaceAll("") + "%0" + phone.substring(4));
    }
}
