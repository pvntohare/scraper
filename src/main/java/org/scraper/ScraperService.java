package org.scraper;

import org.scraper.command.ScrapingCommand;
import org.scraper.command.WebScraper;
import org.scraper.observer.ScraperObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScraperService {
    private final WebScraper webScraper;  // Singleton WebScraper
    private final ExecutorService executorService;
    private final List<ScraperObserver> observers;

    public ScraperService(List<ScraperObserver> observers, int numThreads) {
        webScraper = WebScraper.getInstance();
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.observers = observers;
    }

    // Scrape multiple URLs using multithreading
    public List<Future<String>> scrapeUrls(List<String> urls) {
        List<Future<String>> futures = new ArrayList<>();
        try {
            for (String url : urls) {
                ScrapingCommand command = new ScrapingCommand(url, observers);
                Future<String> future = executorService.submit(command);  // Submit task and capture Future
                futures.add(future);  // Add Future to list
            }
        } finally {
            executorService.shutdown();  // Ensure shutdown even if exceptions occur
        }
        return futures;  // Return the list of Future objects
    }

    // Scrape a single URL
    public String scrapeSingleUrl(String url) throws Exception {
        return webScraper.scrape(url);
    }
}
