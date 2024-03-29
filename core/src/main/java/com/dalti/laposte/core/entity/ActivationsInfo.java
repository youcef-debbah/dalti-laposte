package com.dalti.laposte.core.entity;

import com.dalti.laposte.core.entity.Activation;

import java.util.List;

import dz.jsoftware95.queue.api.ServerResponse;

public class ActivationsInfo extends ServerResponse {
    private List<Activation> activations;
    private int schema;

    public ActivationsInfo() {
    }

    public List<Activation> getActivations() {
        return activations;
    }

    public void setActivations(List<Activation> activations) {
        this.activations = activations;
    }

    public int getSchema() {
        return schema;
    }

    public void setSchema(int schema) {
        this.schema = schema;
    }
}
