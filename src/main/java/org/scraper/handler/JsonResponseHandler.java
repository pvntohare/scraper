package org.scraper.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonResponseHandler implements ResponseHandler {
    private final ObjectMapper objectMapper;

    public JsonResponseHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String handle(String responseBody) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.toString();
    }
}