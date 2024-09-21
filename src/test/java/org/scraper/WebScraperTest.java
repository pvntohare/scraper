package org.scraper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebScraperTest {
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse<String> httpResponse;

    private WebScraper webScraper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        webScraper = new WebScraper(httpClient, new RateLimiter(10), new ObjectMapper());
    }

    @Test
    public void testScrapeHtml() throws IOException, InterruptedException {
        String url = "http://example.com";
        String html = "<html><head><title>My title</title></head><body></body></html>";

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(html);

        String title = webScraper.scrape(url);
        assertEquals("My title", title);
    }

    @Test
    public void testScrapeJson() throws IOException, InterruptedException {
        String url = "http://example.com/entity.json";
        String json = "{\"key\":\"value\"}";

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(json);

        String result = webScraper.scrape(url);
        assertEquals("{\"key\":\"value\"}", result);
    }
}