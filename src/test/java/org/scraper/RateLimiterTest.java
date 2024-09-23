package org.scraper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    public void setUp() {
        rateLimiter = new RateLimiter();
    }

    @Test
    public void testDefaultRateLimit() {
        // Test with default rate limit of 10 requests per second
        String domain = "example.com";

        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.isRequestAllowed(domain), "Request should be allowed");
        }

        // 11th request should be blocked
        assertFalse(rateLimiter.isRequestAllowed(domain), "Request should be blocked due to rate limit");

        // Sleep for 1 second and reset the rate limit
        sleepOneSecond();

        assertTrue(rateLimiter.isRequestAllowed(domain), "Request should be allowed after 1 second");
    }

    @Test
    public void testCustomRateLimitForDomain() {
        // Set custom rate limit for specific domain
        String domain = "example.com";
        rateLimiter.setLimit(domain, 5);  // Custom limit of 5 requests per second

        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isRequestAllowed(domain), "Request should be allowed");
        }

        // 6th request should be blocked
        assertFalse(rateLimiter.isRequestAllowed(domain), "Request should be blocked due to rate limit");

        // Sleep for 1 second to reset rate limit
        sleepOneSecond();

        assertTrue(rateLimiter.isRequestAllowed(domain), "Request should be allowed after 1 second");
    }

    @Test
    public void testDifferentLimitsForDifferentDomains() {
        // Set custom rate limits for different domains
        String domain1 = "example.com";
        String domain2 = "jsonplaceholder.typicode.com";

        rateLimiter.setLimit(domain1, 5);  // Limit 5 for example.com
        rateLimiter.setLimit(domain2, 3);  // Limit 3 for jsonplaceholder.typicode.com

        // Test domain1 (example.com) with 5 requests
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isRequestAllowed(domain1), "Request to example.com should be allowed");
        }
        assertFalse(rateLimiter.isRequestAllowed(domain1), "6th request to example.com should be blocked");

        // Test domain2 (jsonplaceholder.typicode.com) with 3 requests
        for (int i = 0; i < 3; i++) {
            assertTrue(rateLimiter.isRequestAllowed(domain2), "Request to jsonplaceholder should be allowed");
        }
        assertFalse(rateLimiter.isRequestAllowed(domain2), "4th request to jsonplaceholder should be blocked");

        // Sleep for 1 second to reset limits
        sleepOneSecond();

        // After reset, requests should be allowed again
        assertTrue(rateLimiter.isRequestAllowed(domain1), "Request to example.com should be allowed after reset");
        assertTrue(rateLimiter.isRequestAllowed(domain2), "Request to jsonplaceholder should be allowed after reset");
    }

    @Test
    public void testDefaultLimitForUnspecifiedDomain() {
        // Domain without custom limit should use default limit (10 requests)
        String domain = "unspecified.com";

        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.isRequestAllowed(domain), "Request should be allowed");
        }

        // 11th request should be blocked
        assertFalse(rateLimiter.isRequestAllowed(domain), "Request should be blocked due to default rate limit");

        // Sleep for 1 second and reset the rate limit
        sleepOneSecond();

        assertTrue(rateLimiter.isRequestAllowed(domain), "Request should be allowed after 1 second");
    }

    // Helper method to sleep for 1 second and reset the rate limit
    private void sleepOneSecond() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
