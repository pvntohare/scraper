package org.scraper;

import java.io.IOException;
import java.util.List;

public class ScraperService {
    private final WebScraper webScraper;

    public ScraperService(WebScraper webScraper) {
        this.webScraper = webScraper;
    }

    public void scrapeUrls(List<String> urls) {
        for (String url : urls) {
            try {
                String title = webScraper.scrape(url);
                System.out.println("Title: " + title);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}