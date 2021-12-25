package com.dalti.laposte.core.ui;

import android.content.Context;

import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

public enum WebPageDetails {

    CLIENT_PRIVACY_POLICY,

    ADMIN_PRIVACY_POLICY,

    HOW_TO_FIX_GOOGLE_SERVICES,

    ;

    public static final String PAGE_FILE_PREFIX = "web/";
    public static final String PAGE_FILE_SUFFIX = ".html";
    public static final boolean DECOMPRESS = false;

    public String getPageName(Context context) {
        return name().toLowerCase() + "_" + GlobalUtil.getFirstElement(ContextUtils.getPreferredLanguages(context));
    }

    public static String getPageFileName(String pageName) {
        return PAGE_FILE_PREFIX + pageName + PAGE_FILE_SUFFIX;
    }
}
