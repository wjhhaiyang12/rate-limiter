package limiter;

import exception.RateLimiterException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncCountRateLimiterTest {

    @Test
    public void normalTest() throws Exception{
        int threadCount = 10;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        AtomicInteger exceptionCount = new AtomicInteger(0);
        SyncCountRateLimiter rateLimiter = new SyncCountRateLimiter(10, 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int i = 0; i < threadCount; ++i){
            executor.submit(new TestTask(rateLimiter, exceptionCount, countDownLatch));
        }
        countDownLatch.await();
        Assert.assertEquals(0, exceptionCount.get());
    }

    @Test
    public void overCountTest() throws Exception{
        int threadCount = 12;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        AtomicInteger exceptionCount = new AtomicInteger(0);
        SyncCountRateLimiter rateLimiter = new SyncCountRateLimiter(10, 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int i = 0; i < threadCount; ++i){
            executor.submit(new TestTask(rateLimiter, exceptionCount, countDownLatch));
        }
        countDownLatch.await();
        Assert.assertEquals(2, exceptionCount.get());
    }

    @Test
    public void timeWindowNormalTest() throws Exception{
        int threadCount = 10;
        AtomicInteger exceptionCount = new AtomicInteger(0);
        SyncCountRateLimiter rateLimiter = new SyncCountRateLimiter(10, 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int j = 0; j < 5; ++j) {
            CountDownLatch countDownLatch = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; ++i) {
                executor.submit(new TestTask(rateLimiter, exceptionCount, countDownLatch));
            }
            countDownLatch.await();
            Thread.sleep(1000);
        }
        Assert.assertEquals(0, exceptionCount.get());
    }

    @Test
    public void timeWindowOverCountTest() throws Exception{
        int threadCount = 12;
        AtomicInteger exceptionCount = new AtomicInteger(0);
        SyncCountRateLimiter rateLimiter = new SyncCountRateLimiter(10, 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int j = 0; j < 5; ++j) {
            CountDownLatch countDownLatch = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; ++i) {
                executor.submit(new TestTask(rateLimiter, exceptionCount, countDownLatch));
            }
            countDownLatch.await();
            Thread.sleep(1000);
        }
        Assert.assertEquals(10, exceptionCount.get());
    }

    private static class TestTask implements Runnable {

        private final SyncCountRateLimiter rateLimiter;

        private final AtomicInteger exceptionCount;

        private final CountDownLatch countDownLatch;

        public TestTask(SyncCountRateLimiter rateLimiter, AtomicInteger exceptionCount, CountDownLatch countDownLatch) {
            this.rateLimiter = rateLimiter;
            this.exceptionCount = exceptionCount;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                rateLimiter.execute();
            } catch (RateLimiterException e) {
                exceptionCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }
    }

}
