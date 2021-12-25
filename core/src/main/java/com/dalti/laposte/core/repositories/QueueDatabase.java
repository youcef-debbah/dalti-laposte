package com.dalti.laposte.core.repositories;

import androidx.annotation.WorkerThread;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@WorkerThread
@Database(entities = {Service.class, Progress.class, Extra.class, StateEntry.class, Activation.class,
        TurnAlarm.class, AdminAlarm.class, ShortMessage.class, LoggedEvent.class, WebPage.class},
        version = 1)
public abstract class QueueDatabase extends RoomDatabase {

    public static final String NAME = "queue_db";

    public abstract ServiceDAO serviceDAO();

    public abstract ProgressDAO progressDAO();

    public abstract ExtraDAO extraDAO();

    public abstract StateDAO stateDAO();

    public abstract ActivationDAO activationDAO();

    public abstract TurnAlarmDAO turnAlarmDAO();

    public abstract AdminAlarmDAO adminAlarmDAO();

    public abstract ShortMessageDAO shortMessageDAO();

    public abstract LogDAO logDAO();

    public abstract WebPageDAO webPageDAO();
}
