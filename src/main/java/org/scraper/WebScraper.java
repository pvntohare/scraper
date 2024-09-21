package org.scraper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.scraper.handler.HtmlResponseHandler;
import org.scraper.handler.JsonResponseHandler;
import org.scraper.handler.ResponseHandler;

public class WebScraper {
    private final HttpClient httpClient;
    private final RateLimiter rateLimiter;
    private final Map<String, ResponseHandler> handlers;

    public WebScraper() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build(), new RateLimiter(10), new ObjectMapper());
    }

    public WebScraper(HttpClient httpClient, RateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.rateLimiter = rateLimiter;
        this.handlers = new HashMap<>();
        this.handlers.put("text/html", new HtmlResponseHandler());
        this.handlers.put("application/json", new JsonResponseHandler(objectMapper));
    }

    public String scrape(String url) throws IOException, InterruptedException {
        rateLimiter.acquire();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String contentType = response.headers().firstValue("Content-Type").orElse("text/html");
        ResponseHandler handler = handlers.get(contentType.split(";")[0]);
        if (handler == null) {
            throw new UnsupportedOperationException("Unsupported content type: " + contentType);
        }
        return handler.handle(response.body());
    }
}