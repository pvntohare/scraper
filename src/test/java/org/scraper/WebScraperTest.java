package org.scraper;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.scraper.handler.ResponseHandlerFactory;

public class WebScraperTest {

    @Mock
    private HttpClient mockHttpClient;
    @Mock
    private HttpResponse<String> mockResponse;

    private WebScraper webScraper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        webScraper = new WebScraper(mockHttpClient, new RateLimiter(10), new ObjectMapper(), 10);
    }

    @Test
    public void testScrapeJsonResponse() throws IOException, InterruptedException, ExecutionException {
        String jsonResponse = "{\"title\": \"My title\"}";

        // Mock HTTP client behavior
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        when(mockResponse.headers()).thenReturn(HttpHeadersMock.create("application/json"));
        when(mockResponse.body()).thenReturn(jsonResponse);

        String result = webScraper.scrape("https://example.com/entity-abc-123.json");
        assertEquals("{\"title\": \"My title\"}", result);
    }

    @Test
    public void testScrapeHtmlResponse() throws IOException, InterruptedException, ExecutionException {
        String htmlResponse = "<html><body><h1 class='product-title' data-id='aksjd76asd-sad3-saj09jso'>Product Title</h1></body></html>";

        // Mock HTTP client behavior
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        when(mockResponse.headers()).thenReturn(HttpHeadersMock.create("text/html"));
        when(mockResponse.body()).thenReturn(htmlResponse);

        String result = webScraper.scrape("https://example.com/product-abc.html");
        assertEquals("Product Title", result);
    }
}
