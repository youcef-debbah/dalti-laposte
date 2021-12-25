package com.dalti.laposte.core.repositories;

import java.util.List;

import dz.jsoftware95.queue.response.ServerResponse;

public class ServicesInfo extends ServerResponse {
    private List<Service> services;
    private int schema;

    public ServicesInfo() {
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public int getSchema() {
        return schema;
    }

    public void setSchema(int schema) {
        this.schema = schema;
    }
}
