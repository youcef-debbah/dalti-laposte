package com.dalti.laposte.core.repositories;

import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.Test;

import dz.jsoftware95.queue.api.ServerResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class JsonParsingTest {

    private static final String OLD_SERVER_TARGET = "{\"message\":null,\"data\":{\"migration-date\":\"1619512945000\"},\"code\":1,\"topic\":null,\"schema\":0}";

    @Test
    public void testAVG() throws JsonProcessingException {
        Object parsedObject = AbstractQueueApplication.parse(OLD_SERVER_TARGET, ServerResponse.class);
        System.out.println(parsedObject);
        assertThat(parsedObject, notNullValue());
    }
}