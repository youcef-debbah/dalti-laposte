package com.dalti.laposte.core.repositories;

import androidx.annotation.WorkerThread;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.dalti.laposte.core.entity.Activation;
import com.dalti.laposte.core.entity.AdminAlarm;
import com.dalti.laposte.core.entity.Extra;
import com.dalti.laposte.core.entity.LoggedEvent;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.entity.Service;
import com.dalti.laposte.core.entity.ShortMessage;
import com.dalti.laposte.core.entity.StateEntry;
import com.dalti.laposte.core.entity.TurnAlarm;
import com.dalti.laposte.core.entity.WebPage;

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
