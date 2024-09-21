package org.scraper;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

public class HttpHeadersMock {
    public static HttpHeaders create(String contentType) {
        return HttpHeaders.of(Map.of("Content-Type", List.of(contentType)), (key, value) -> true);
    }
}
