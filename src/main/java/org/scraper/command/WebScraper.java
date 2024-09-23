package org.scraper.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.scraper.RateLimiter;
import org.scraper.handler.ResponseHandler;
import org.scraper.handler.ResponseHandlerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WebScraper {

    // Static instance of the singleton class
    private static WebScraper instance;

    private final HttpClient httpClient;
    private final RateLimiter rateLimiter;
    private final ResponseHandlerFactory handlerFactory;

    // Private constructor to prevent instantiation from other classes
    private WebScraper(HttpClient httpClient, RateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.rateLimiter = rateLimiter;
        this.handlerFactory = new ResponseHandlerFactory(objectMapper);
    }

    // Static method to get the singleton instance of WebScraper
    public static WebScraper getInstance() {
        if (instance == null) {
            synchronized (WebScraper.class) {  // Thread-safe singleton initialization
                if (instance == null) {
                    // Initialize dependencies
                    HttpClient httpClient = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(10))
                            .build();
                    RateLimiter rateLimiter = new RateLimiter();
                    ObjectMapper objectMapper = new ObjectMapper();

                    // Set custom rate limits for specific domains
                    // TODO : Add config for domain specific limit. We can have default limit for all domains,
                    //  and add per domain if required
                    rateLimiter.setLimit("example.com", 5);  // Max 5 requests per second
                    rateLimiter.setLimit("jsonplaceholder.typicode.com", 2);  // Max 2 requests per second
                    rateLimiter.setLimit("api.github.com", 3);  // Max 3 requests per second

                    instance = new WebScraper(httpClient, rateLimiter, objectMapper);
                }
            }
        }
        return instance;
    }

    // Method to reset the singleton for testing
    public static void resetInstance(HttpClient httpClient, RateLimiter rateLimiter, ObjectMapper objectMapper) {
        instance = new WebScraper(httpClient, rateLimiter, objectMapper);
    }

    // Method to scrape a single URL
    public String scrape(String url) throws IOException, InterruptedException, URISyntaxException {
        String domain = extractDomainFromUrl(url);

        if (!rateLimiter.isRequestAllowed(domain)) {
            System.err.println("Rate limit exceeded for domain: " + domain);
            return null;  // Optionally handle rate limit exceeded scenarios
        }

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

    // Helper method to extract the domain from a URL
    private String extractDomainFromUrl(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return uri.getHost();
    }
}
