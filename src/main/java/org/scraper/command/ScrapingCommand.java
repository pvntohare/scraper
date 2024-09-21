package org.scraper.command;

import org.scraper.ScraperService;
import org.scraper.observer.ScraperObserver;

import java.util.List;
import java.util.concurrent.Callable;

public class ScrapingCommand implements Callable<String> {
    private final String url;
    private final ScraperService scraperService;
    private final List<ScraperObserver> observers;

    public ScrapingCommand(String url, ScraperService scraperService, List<ScraperObserver> observers) {
        this.url = url;
        this.scraperService = scraperService;
        this.observers = observers;
    }

    @Override
    public String call() {
        try {
            notifyObserversStart();
            String result = scraperService.scrapeSingleUrl(url);  // Executes the scraping
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
