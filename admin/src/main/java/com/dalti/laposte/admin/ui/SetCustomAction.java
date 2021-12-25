package com.dalti.laposte.admin.ui;

import com.dalti.laposte.core.repositories.AdminAction;
import com.dalti.laposte.core.repositories.Progress;
import com.dalti.laposte.core.repositories.Service;

class SetCustomAction implements AdminAction {

    private final int current;
    private final int waiting;
    private final Integer availability;

    public SetCustomAction(int current, int waiting, Integer availability) {
        this.current = current;
        this.waiting = waiting;
        this.availability = availability;
    }

    @Override
    public void apply(Progress progress) {
        if (progress != null) {
            progress.setCurrentToken(current);
            progress.setWaiting(waiting);
            AdminAction.validate(progress);
        }
    }

    @Override
    public void apply(Service service) {
        if (service != null)
            service.setAvailability(availability);
    }
}
