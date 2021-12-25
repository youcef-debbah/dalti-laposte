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

package dz.jsoftware95.silverbox.android.backend;

import dz.jsoftware95.silverbox.android.common.StringUtil;

/**
 * An entity that it's instances are distinguishable by a unique id.
 */
public interface Item {
    /**
     * The ID that will cause SQLite database engine to generate an ID automatically.
     */
    long AUTO_ID = 0;

    /**
     * Returns the id of this Item (used to detect whether two Items are the same)
     *
     * @return the id of this Item
     */
    long getId();

    /**
     * Checks whether <code>this</code> item has an ID different than {@link #AUTO_ID}
     */
    default void ensurePersisted() {
        if (getId() == AUTO_ID)
            throw new IllegalStateException("the entity is not persisted");
    }

    default int idHashcode() {
        return StringUtil.hash(getId());
    }

    default boolean isValid() {
        return getId() != AUTO_ID;
    }
}
