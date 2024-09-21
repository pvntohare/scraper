package org.scraper;

import java.util.concurrent.Semaphore;

public class RateLimiter {
    private final Semaphore semaphore;

    public RateLimiter(int permitsPerSecond) {
        this.semaphore = new Semaphore(permitsPerSecond);
    }

    public void acquire() throws InterruptedException {
        semaphore.acquire();
        semaphore.release();
    }
}