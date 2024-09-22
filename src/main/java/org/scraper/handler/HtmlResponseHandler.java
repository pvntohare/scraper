package org.scraper.handler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlResponseHandler implements ResponseHandler {
    @Override
    public String handle(String responseBody) {
        Document doc = Jsoup.parse(responseBody);
        return doc.select("h1.product-title").text();
    }
}