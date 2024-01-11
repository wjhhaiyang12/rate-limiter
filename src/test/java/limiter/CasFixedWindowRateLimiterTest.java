package limiter;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CasFixedWindowRateLimiterTest {

    @Test
    public void normalTest() throws Exception{
        int threadCount = 100;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        CasFixedWindowRateLimiter rateLimiter = new CasFixedWindowRateLimiter(100, 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int i = 0; i < threadCount; ++i){
            executor.submit(new TestTask(rateLimiter, countDownLatch));
        }
        countDownLatch.await();
        Assert.assertEquals(1, rateLimiter.getInitializeCount());
        Assert.assertEquals(0, rateLimiter.getRefreshCount());
        Assert.assertEquals(0, rateLimiter.getExceptionCount());
        Assert.assertEquals(100, rateLimiter.getCount());
    }

    @Test
    public void overCountTest() throws Exception{
        int threadCount = 120;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        CasFixedWindowRateLimiter rateLimiter = new CasFixedWindowRateLimiter(100, 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int i = 0; i < threadCount; ++i){
            executor.submit(new TestTask(rateLimiter, countDownLatch));
        }
        countDownLatch.await();
        Assert.assertEquals(1, rateLimiter.getInitializeCount());
        Assert.assertEquals(0, rateLimiter.getRefreshCount());
        Assert.assertEquals(20, rateLimiter.getExceptionCount());
        Assert.assertEquals(100, rateLimiter.getCount());
    }

    @Test
    public void timeWindowNormalTest() throws Exception{
        int threadCount = 100;
        CasFixedWindowRateLimiter rateLimiter = new CasFixedWindowRateLimiter(100, 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int j = 0; j < 5; ++j) {
            CountDownLatch countDownLatch = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; ++i) {
                executor.submit(new TestTask(rateLimiter, countDownLatch));
            }
            countDownLatch.await();
            Thread.sleep(1100);
        }
        Assert.assertEquals(1, rateLimiter.getInitializeCount());
        Assert.assertEquals(4, rateLimiter.getRefreshCount());
        Assert.assertEquals(0, rateLimiter.getExceptionCount());
        Assert.assertEquals(100, rateLimiter.getCount());
    }

    @Test
    public void timeWindowOverCountTest() throws Exception{
        int threadCount = 120;
        CasFixedWindowRateLimiter rateLimiter = new CasFixedWindowRateLimiter(100, 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int j = 0; j < 5; ++j) {
            CountDownLatch countDownLatch = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; ++i) {
                executor.submit(new TestTask(rateLimiter, countDownLatch));
            }
            countDownLatch.await();
            Thread.sleep(1000);
        }
        Assert.assertEquals(1, rateLimiter.getInitializeCount());
        Assert.assertEquals(4, rateLimiter.getRefreshCount());
        Assert.assertEquals(100, rateLimiter.getExceptionCount());
        Assert.assertEquals(100, rateLimiter.getCount());
    }
}
