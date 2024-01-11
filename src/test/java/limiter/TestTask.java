package limiter;

import java.util.concurrent.CountDownLatch;

public class TestTask implements Runnable{

    private final RateLimiter rateLimiter;

    private final CountDownLatch countDownLatch;

    public TestTask(RateLimiter rateLimiter, CountDownLatch countDownLatch) {
        this.rateLimiter = rateLimiter;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            rateLimiter.execute();
        } finally {
            countDownLatch.countDown();
        }
    }

}
