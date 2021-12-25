/*
 * Copyright (c) 2018 Youcef DEBBAH (youcef-debbah@hotmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the Software) to deal in the Software without restriction
 * but under the following conditions:
 *
 * - This notice shall be included in all copies and portions of the Software.
 * - The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND (Implicit or Explicit).
 *
 */

package dz.jsoftware95.silverbox.android.common;

import javax.annotation.concurrent.Immutable;

/**
 * Build configurations that concern classes in this package
 */
@Immutable
public final class Config {

    private Config() {
        throw new UnsupportedOperationException("bad boy! no instance for you");
    }

    /**
     * Indicates whether assertions (like the assertions in the {@link Assert} class)
     * should be left or optimised away
     */
    public static final boolean ASSERTION_ENABLED = true;

    /**
     * Indicates whether log output that is intended mainly as a debug info
     */
    public static final boolean LOG_DEBUG_INFO = true;
}
