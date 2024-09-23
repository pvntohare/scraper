package org.scraper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.scraper.command.WebScraper;
import org.scraper.observer.LoggingObserver;
import org.scraper.observer.ScraperObserver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebScraperTest {

    @Mock
    private HttpClient mockHttpClient;
    @Mock
    private HttpResponse<String> mockResponse;

    private WebScraper webScraper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        RateLimiter rateLimiter = new RateLimiter();
        rateLimiter.setLimit("example.com", 5);  // Max 5 requests per second
        rateLimiter.setLimit("jsonplaceholder.typicode.com", 2);  // Max 2 requests per second
        rateLimiter.setLimit("api.github.com", 3);  // Max 3 requests per second
        WebScraper.resetInstance(mockHttpClient, new RateLimiter(), new ObjectMapper());
        webScraper = WebScraper.getInstance();
    }

    @Test
    void testScrapeJsonResponse() throws IOException, InterruptedException, ExecutionException, URISyntaxException {
        String jsonResponse = "{\"title\": \"My title\"}";

        // Mock HTTP client behavior
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        when(mockResponse.headers()).thenReturn(HttpHeadersMock.create("application/json"));
        when(mockResponse.body()).thenReturn(jsonResponse);

        String result = webScraper.scrape("https://example.com/entity-abc-123.json");
        assertEquals("My title", result);
    }

    @Test
    void testScrapeHtmlResponse() throws IOException, InterruptedException, URISyntaxException {
        String htmlResponse = "<html><body><h1 class='product-title' data-id='aksjd76asd-sad3-saj09jso'>Product Title</h1></body></html>";

        // Mock HTTP client behavior
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
        when(mockResponse.headers()).thenReturn(HttpHeadersMock.create("text/html"));
        when(mockResponse.body()).thenReturn(htmlResponse);

        String result = webScraper.scrape("https://example.com/product-abc.html");
        assertEquals("Product Title", result);
    }

    @Test
    public void testScrapeAsyncMultipleUrlsWithScraperService() throws Exception {

        String htmlResponse1 = "<html><body><h1 class='product-title' data-id='aksjd76asd-sad3-saj09jso'>Product Title 1</h1></body></html>";
        String htmlResponse2 = "<html><body><h1 class='product-title' data-id='aksjd76asd-sad3-saj09jso'>Product Title 2</h1></body></html>";
        String htmlResponse3 = "<html><body><h1 class='product-title' data-id='aksjd76asd-sad3-saj09jso'>Product Title 3</h1></body></html>";

        // Mock HTTP client behavior for multiple URLs
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        when(mockResponse.headers()).thenReturn(HttpHeadersMock.create("text/html"));

        when(mockResponse.body())
                .thenReturn(htmlResponse1)  // First URL response
                .thenReturn(htmlResponse2)  // Second URL response
                .thenReturn(htmlResponse3); // Third URL response

        // Define the URLs to scrape
        List<String> urls = Arrays.asList(
                "https://example.com/product-abc1.html",
                "https://example.com/product-abc2.html",
                "https://example.com/product-abc3.html"
        );

        // Create ScraperService with a mocked LoggingObserver
        ScraperObserver loggingObserver = mock(LoggingObserver.class);
        ScraperService scraperService = new ScraperService(Arrays.asList(loggingObserver), 3);

        // Use scrapeUrls to scrape multiple URLs asynchronously
        List<Future<String>> futures = scraperService.scrapeUrls(urls);

        // Collect and verify the results
        List<String> results = new ArrayList<>();

        for (Future<String> future : futures) {
            results.add(future.get());  // Wait for each async task to complete and collect result
        }

        // Define the expected results (order doesn't matter)
        List<String> expectedResults = Arrays.asList(
                "Product Title 1",
                "Product Title 2",
                "Product Title 3"
        );

        // Sort both lists before comparison to avoid ordering issues
        results.sort(String::compareTo);
        expectedResults.sort(String::compareTo);

        // Verify that all results match, irrespective of order
        assertEquals(expectedResults, results);

        // Verify observer notifications
        for (String url : urls) {
            verify(loggingObserver, timeout(1000)).onScrapeStarted(url);
        }
    }


    // Test failure scenario
    @Test
    void testObserverFailureScenario() throws Exception {
        // Mock a failure in the HTTP client
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Failed to fetch data"));

        // Mock LoggingObserver
        ScraperObserver loggingObserver = mock(LoggingObserver.class);

        // Create ScraperService with a single thread for testing
        ScraperService scraperService = new ScraperService(Arrays.asList(loggingObserver), 1);

        // Use scrapeUrls to ensure asynchronous execution
        scraperService.scrapeUrls(List.of("https://example.com/failing-url"));

        // Verify observer notifications for failure
        verify(loggingObserver, timeout(1000)).onScrapeStarted("https://example.com/failing-url");
        verify(loggingObserver, timeout(1000)).onScrapeFailed(eq("https://example.com/failing-url"), any(IOException.class));
    }


    @Test
    void testScraperServiceWithLoggingObserver() throws Exception {
        String jsonResponse = "{\"title\": \"My title\"}";

        // Mock HTTP client behavior
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.headers()).thenReturn(HttpHeadersMock.create("application/json"));
        when(mockResponse.body()).thenReturn(jsonResponse);

        // Mock LoggingObserver
        ScraperObserver loggingObserver = mock(LoggingObserver.class);

        // Create ScraperService with a single thread for testing
        ScraperService scraperService = new ScraperService(Arrays.asList(loggingObserver), 1);

        // Use scrapeUrls to ensure asynchronous execution
        scraperService.scrapeUrls(List.of("https://example.com/entity-abc-123.json"));

        // Verify observer notifications
        verify(loggingObserver, timeout(1000)).onScrapeStarted("https://example.com/entity-abc-123.json");
        verify(loggingObserver, timeout(1000)).onScrapeSuccess(eq("https://example.com/entity-abc-123.json"), eq("My title"));
    }


}
