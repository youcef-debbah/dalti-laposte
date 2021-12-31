package com.dalti.laposte.admin.ui;

import com.dalti.laposte.core.repositories.AdminAction;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.entity.Service;

public enum StaticAdminAction implements AdminAction {
    NONE,
    INC_CURRENT {
        @Override
        public void mutate(Progress progress) {
            progress.setCurrentToken(progress.getCurrentTokenInt() + 1);
            progress.setWaiting(progress.getWaitingInt() - 1);
        }
    },
    INC_WAITING {
        @Override
        public void mutate(Progress progress) {
            progress.setWaiting(progress.getWaitingInt() + 1);
        }
    },
    ;

    public void apply(Progress progress) {
        if (progress != null) {
            mutate(progress);
            AdminAction.validate(progress);
        }
    }

    public void apply(Service service) {
        if (service != null)
            mutate(service);
    }

    protected void mutate(Progress progress) {
    }

    protected void mutate(Service progress) {
    }
}
