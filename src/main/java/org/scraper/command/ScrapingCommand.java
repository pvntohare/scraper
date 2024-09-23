package org.scraper.command;

import org.scraper.observer.ScraperObserver;

import java.util.List;
import java.util.concurrent.Callable;

public class ScrapingCommand implements Callable<String> {
    private final WebScraper webScraper;  // Singleton WebScraper
    private final String url;
    private final List<ScraperObserver> observers;

    public ScrapingCommand(String url, List<ScraperObserver> observers) {
        webScraper = WebScraper.getInstance();
        this.url = url;
        this.observers = observers;
    }

    @Override
    public String call() {
        try {
            notifyObserversStart();
            String result = webScraper.scrape(url);  // Executes the scraping
            notifyObserversSuccess(result);
            return result;
        } catch (Exception e) {
            notifyObserversFailure(e);
            return null;
        }
    }

    private void notifyObserversStart() {
        for (ScraperObserver observer : observers) {
            observer.onScrapeStarted(url);
        }
    }

    private void notifyObserversSuccess(String result) {
        for (ScraperObserver observer : observers) {
            observer.onScrapeSuccess(url, result);
        }
    }

    private void notifyObserversFailure(Exception e) {
        for (ScraperObserver observer : observers) {
            observer.onScrapeFailed(url, e);
        }
    }
}
