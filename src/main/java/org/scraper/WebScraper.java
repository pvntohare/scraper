package org.scraper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.scraper.handler.ResponseHandler;
import org.scraper.handler.ResponseHandlerFactory;

public class WebScraper {
    private final HttpClient httpClient;
    private final RateLimiter rateLimiter;
    private final ResponseHandlerFactory handlerFactory;

    public WebScraper() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build(), new RateLimiter(10), new ObjectMapper());
    }

    public WebScraper(HttpClient httpClient, RateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.rateLimiter = rateLimiter;
        this.handlerFactory = new ResponseHandlerFactory(objectMapper);  // Use the factory
    }

    public String scrape(String url) throws IOException, InterruptedException {
        rateLimiter.acquire();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String contentType = response.headers().firstValue("Content-Type").orElse("text/html").split(";")[0];

        ResponseHandler handler = handlerFactory.getHandler(contentType);  // Get the appropriate handler
        if (handler == null) {
            throw new UnsupportedOperationException("Unsupported content type: " + contentType);
        }
        return handler.handle(response.body());
    }
}