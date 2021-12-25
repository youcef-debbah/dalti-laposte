package com.dalti.laposte.core.repositories;

import java.util.List;
import java.util.Map;

import dz.jsoftware95.queue.response.ServerResponse;

public class AlarmsInfo extends ServerResponse {
    private List<AdminAlarm> alarms;
    private int schema;

    public AlarmsInfo() {
    }

    public AlarmsInfo(Map<String, String> data, int code) {
        super(data, code);
    }

    public List<AdminAlarm> getAlarms() {
        return alarms;
    }

    public void setAlarms(List<AdminAlarm> alarms) {
        this.alarms = alarms;
    }

    public int getSchema() {
        return schema;
    }

    public void setSchema(int schema) {
        this.schema = schema;
    }
}
