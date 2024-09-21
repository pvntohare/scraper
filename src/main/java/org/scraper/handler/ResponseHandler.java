package org.scraper.handler;

import java.io.IOException;

public interface ResponseHandler {
    String handle(String responseBody) throws IOException;
}