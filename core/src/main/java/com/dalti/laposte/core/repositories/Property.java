package com.dalti.laposte.core.repositories;

import dz.jsoftware95.silverbox.android.common.StringUtil;

public interface Property {
    int ID_1 = 1;
    int ID_3 = 3;
    int ID_8 = 8;
    int ID_16 = 16;
    int ID_17 = 17;
    int ID_18 = 18;
    int ID_19 = 19;
    int ID_108 = 108;
    int ID_109 = 109;
    int ID_110 = 110;
    int ID_111 = 111;

    int ID_2 = 2;
    int ID_112 = 112;
    int ID_113 = 113;
    int ID_114 = 114;
    int ID_115 = 115;
    int ID_116 = 116;
    int ID_117 = 117;
    int ID_118 = 118;
    int ID_119 = 119;

    long key();

    default boolean isNull(String value) {
        return StringUtil.isNullOrEmpty(value);
    }

    default boolean isNotNull(String value) {
        return !isNull(value);
    }
}
