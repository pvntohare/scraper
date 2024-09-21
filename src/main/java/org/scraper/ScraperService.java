package org.scraper;

import org.scraper.command.ScrapingCommand;
import org.scraper.observer.ScraperObserver;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScraperService {
    private final WebScraper webScraper;
    private final ExecutorService executorService;
    private final List<ScraperObserver> observers;

    public ScraperService(WebScraper webScraper, List<ScraperObserver> observers, int numThreads) {
        this.webScraper = webScraper;
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.observers = observers;
    }

    // Scrape multiple URLs using multithreading
    public void scrapeUrls(List<String> urls) {
        for (String url : urls) {
            ScrapingCommand command = new ScrapingCommand(url, this, observers);
            Future<String> future = executorService.submit(command);  // Executes the command asynchronously
        }
        executorService.shutdown();  // Graceful shutdown of the thread pool
    }

    // Scrape a single URL
    public String scrapeSingleUrl(String url) throws Exception {
        return webScraper.scrape(url);
    }
}
