package org.scraper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private final ConcurrentHashMap<String, RateLimitInfo> domainLimits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> customLimits = new ConcurrentHashMap<>();

    private static class RateLimitInfo {
        private int requests;
        private long lastRequestTime;

        public RateLimitInfo() {
            this.requests = 0;
            this.lastRequestTime = System.currentTimeMillis();
        }

        public synchronized boolean allowRequest(int limit) {
            long now = System.currentTimeMillis();
            if (now - lastRequestTime > TimeUnit.SECONDS.toMillis(1)) {
                // Reset the counter and timestamp if a second has passed
                requests = 0;
                lastRequestTime = now;
            }
            if (requests < limit) { // Allow requests based on the limit provided
                requests++;
                return true;
            }
            return false;
        }
    }

    public void setLimit(String domain, int limit) {
        customLimits.put(domain, limit);
    }

    public boolean isRequestAllowed(String domain) {
        int limit = customLimits.getOrDefault(domain, 10); // Default limit if not set
        return domainLimits.computeIfAbsent(domain, k -> new RateLimitInfo()).allowRequest(limit);
    }
}