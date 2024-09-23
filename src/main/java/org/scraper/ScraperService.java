package org.scraper;

import org.scraper.command.ScrapingCommand;
import org.scraper.command.WebScraper;
import org.scraper.observer.ScraperObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class ScraperService {
    private final WebScraper webScraper;  // Singleton WebScraper
    private final ExecutorService executorService;
    private final List<ScraperObserver> observers;
    // This queue will be used to hold the request as we might be processing millions of requests,
    // and we don't want to overload our system and still process all the requests
    private final BlockingQueue<String> requestQueue;
    private final int numThreads;

    public ScraperService(List<ScraperObserver> observers, int numThreads) {
        webScraper = WebScraper.getInstance();
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.observers = observers;
        this.requestQueue = new LinkedBlockingQueue<>(); // Initialize the queue
        this.numThreads = numThreads;
    }

    // Method to add URLs to the queue
    public void queueUrls(List<String> urls) {
        requestQueue.addAll(urls); // Add URLs to the queue
    }

    // Start processing requests from the queue
    public void startProcessing() {
        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                while (true) {
                    try {
                        String url = requestQueue.take();
                        ScrapingCommand command = new ScrapingCommand(url, observers);
                        command.call();
                    } catch (InterruptedException e) {
                        // Restore the interrupted status and exit the loop if interrupted
                        Thread.currentThread().interrupt();
                        System.out.println("Thread interrupted, stopping processing.");
                        break; // Exit the loop on interruption
                    } catch (Exception e) {
                        System.err.println("Error while scraping URL: " + e.getMessage());
                    }
                }
            });
        }
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
