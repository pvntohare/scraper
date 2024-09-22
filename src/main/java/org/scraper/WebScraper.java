package org.scraper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.scraper.handler.ResponseHandler;
import org.scraper.handler.ResponseHandlerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WebScraper {
    private final HttpClient httpClient;
    private final RateLimiter rateLimiter;
    private final ResponseHandlerFactory handlerFactory;
    private final ExecutorService executorService;

    public WebScraper(int numThreads) {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build(), new RateLimiter(10), new ObjectMapper(), numThreads);
    }

    public WebScraper(HttpClient httpClient, RateLimiter rateLimiter, ObjectMapper objectMapper, int numThreads) {
        this.httpClient = httpClient;
        this.rateLimiter = rateLimiter;
        this.handlerFactory = new ResponseHandlerFactory(objectMapper);
        this.executorService = Executors.newFixedThreadPool(numThreads); // Thread pool
    }

    public Future<String> scrapeAsync(String url) {
        return executorService.submit(() -> scrape(url));
    }

    public String scrape(String url) throws IOException, InterruptedException {
        rateLimiter.acquire();  // Apply rate-limiting logic
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String contentType = response.headers().firstValue("Content-Type").orElse("text/html").split(";")[0];

        ResponseHandler handler = handlerFactory.getHandler(contentType);
        if (handler == null) {
            throw new UnsupportedOperationException("Unsupported content type: " + contentType);
        }
        return handler.handle(response.body());
    }

    public void shutdown() {
        executorService.shutdown();
    }
}