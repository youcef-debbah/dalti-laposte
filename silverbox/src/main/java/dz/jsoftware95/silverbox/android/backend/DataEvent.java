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

import androidx.annotation.AnyThread;

/**
 * Backend events that are related to the stored data of the application
 */
@AnyThread
public enum DataEvent implements BackendEvent {

    FETCHING_DATA {
        @Override
        public boolean shouldStartRefreshing() {
            return true;
        }
    },
    DATA_FETCHED {
        @Override
        public boolean shouldStopRefreshing() {
            return true;
        }
    },
    SUCCESSFUL_UPDATE {
        @Override
        public boolean shouldStopRefreshing() {
            return true;
        }
    }
}
