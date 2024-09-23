package org.scraper;

import org.scraper.observer.LoggingObserver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws Exception {
        // Create ScraperService with LoggingObserver
        ScraperService scraperService = new ScraperService(List.of(new LoggingObserver()), 10);

        // Example list of URLs to scrape from different domains
        List<String> urls = Arrays.asList(
                "https://example.com", // HTML response
                "https://jsonplaceholder.typicode.com/posts/1", // JSON response
                "https://api.github.com/users/octocat" // JSON response
        );

        // Start scraping asynchronously and get futures
        List<Future<String>> futures = scraperService.scrapeUrls(urls);

        // Collect and print the results after scraping
        for (Future<String> future : futures) {
            String result = future.get();  // Wait for the task to complete and get the result
            System.out.println("Scraped content: " + result);
        }
    }
}
