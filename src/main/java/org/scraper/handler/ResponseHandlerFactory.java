package org.scraper.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandlerFactory {

    private final Map<String, ResponseHandler> handlerRegistry = new HashMap<>();

    public ResponseHandlerFactory(ObjectMapper objectMapper) {
        // Registering response handlers for content types
        registerHandler("text/html", new HtmlResponseHandler());
        registerHandler("application/json", new JsonResponseHandler(objectMapper));
        // You can register additional response handlers here in the future
    }

    public void registerHandler(String contentType, ResponseHandler handler) {
        handlerRegistry.put(contentType, handler);
    }

    public ResponseHandler getHandler(String contentType) {
        return handlerRegistry.get(contentType);
    }
}
