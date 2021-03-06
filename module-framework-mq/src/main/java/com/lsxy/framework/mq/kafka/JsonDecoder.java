package com.lsxy.framework.mq.kafka;

import java.io.IOException;
import java.util.Map;

import kafka.serializer.Decoder;
import kafka.utils.VerifiableProperties;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDecoder  implements Decoder<Object> {
    private static final Logger LOGGER = Logger.getLogger(JsonEncoder.class);
    public JsonDecoder(VerifiableProperties verifiableProperties) {
        /* This constructor must be present for successful compile. */
    }

    @Override
    public Object fromBytes(byte[] bytes) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(bytes, Map.class);
        } catch (IOException e) {
            LOGGER.error(String.format("Json processing failed for object: %s", bytes.toString()), e);
        }
        return null;
    }
}