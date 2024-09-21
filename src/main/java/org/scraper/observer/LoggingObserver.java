package org.scraper.observer;

public class LoggingObserver implements ScraperObserver {

    @Override
    public void onScrapeStarted(String url) {
        System.out.println("Scraping started for URL: " + url);
    }

    @Override
    public void onScrapeSuccess(String url, String result) {
        System.out.println("Scraping succeeded for URL: " + url + " with result: " + result);
    }

    @Override
    public void onScrapeFailed(String url, Exception e) {
        System.err.println("Scraping failed for URL: " + url + " with error: " + e.getMessage());
    }
}
