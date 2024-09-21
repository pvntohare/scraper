package org.scraper.observer;

public interface ScraperObserver {
    void onScrapeStarted(String url);
    void onScrapeSuccess(String url, String result);
    void onScrapeFailed(String url, Exception e);
}
