package org.scraper;

import org.scraper.command.ScrapingCommand;
import org.scraper.observer.ScraperObserver;

import java.util.ArrayList;
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
    public List<Future<String>> scrapeUrls(List<String> urls) {
        List<Future<String>> futures = new ArrayList<>();
        for (String url : urls) {
            ScrapingCommand command = new ScrapingCommand(url, this, observers);
            Future<String> future = executorService.submit(command);  // Executes the command asynchronously
            futures.add(future);
        }
//        for (Future<String> future : futures) {
//            try {
//                String result = future.get();  // Wait for task to complete and get result
//                System.out.println("Scraped Result: " + result);
//            } catch (Exception e) {
//                System.err.println("Error processing scrape result: " + e.getMessage());
//                // Handle exceptions if needed (e.g., retry failed tasks)
//            }
//        }

        executorService.shutdown();  // Graceful shutdown of the thread pool
        return futures;
    }

    // Scrape a single URL
    public String scrapeSingleUrl(String url) throws Exception {
        return webScraper.scrape(url);
    }
}
