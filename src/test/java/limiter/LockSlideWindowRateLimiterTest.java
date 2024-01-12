package limiter;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LockSlideWindowRateLimiterTest {

    @Test
    public void normalTest() throws Exception{
        int threadCount = 100;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
            LockSlideWindowRateLimiter rateLimiter = new LockSlideWindowRateLimiter(100, 5, 5, TimeUnit.SECONDS);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int i = 0; i < threadCount; ++i){
            executor.submit(new TestTask(rateLimiter, countDownLatch));
        }
        countDownLatch.await();
        Assert.assertEquals(1, rateLimiter.getInitializeCount());
        Assert.assertEquals(0, rateLimiter.getForwardCount());
        Assert.assertEquals(0, rateLimiter.getExceptionCount());
        Assert.assertEquals(100, rateLimiter.getCount());
    }

    @Test
    public void exceptionTest() throws Exception{
        int threadCount = 120;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        LockSlideWindowRateLimiter rateLimiter = new LockSlideWindowRateLimiter(100, 5, 5, TimeUnit.SECONDS);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int i = 0; i < threadCount; ++i){
            executor.submit(new TestTask(rateLimiter, countDownLatch));
        }
        countDownLatch.await();
        Assert.assertEquals(1, rateLimiter.getInitializeCount());
        Assert.assertEquals(0, rateLimiter.getForwardCount());
        Assert.assertEquals(20, rateLimiter.getExceptionCount());
        Assert.assertEquals(100, rateLimiter.getCount());
    }

    @Test
    public void timeIntervalTest() throws Exception{
        int threadCount = 20;
        LockSlideWindowRateLimiter rateLimiter = new LockSlideWindowRateLimiter(100, 5, 5, TimeUnit.SECONDS);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int j = 0; j < 10; ++j) {
            CountDownLatch countDownLatch = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; ++i) {
                executor.submit(new TestTask(rateLimiter, countDownLatch));
            }
            countDownLatch.await();
            Thread.sleep(1000);
        }
        Assert.assertEquals(1, rateLimiter.getInitializeCount());
        Assert.assertEquals(9, rateLimiter.getForwardCount());
        Assert.assertEquals(0, rateLimiter.getExceptionCount());
        Assert.assertEquals(100, rateLimiter.getCount());
    }

    @Test
    public void timeIntveralExceptionTest() throws Exception{
        int threadCount = 30;
        LockSlideWindowRateLimiter rateLimiter = new LockSlideWindowRateLimiter(100, 5, 5, TimeUnit.SECONDS);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int j = 0; j < 10; ++j) {
            CountDownLatch countDownLatch = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; ++i) {
                executor.submit(new TestTask(rateLimiter, countDownLatch));
            }
            countDownLatch.await();
            Thread.sleep(1000);
        }
        Assert.assertEquals(1, rateLimiter.getInitializeCount());
        Assert.assertEquals(9, rateLimiter.getForwardCount());
        Assert.assertEquals(100, rateLimiter.getExceptionCount());
        Assert.assertEquals(100, rateLimiter.getCount());
    }

    @Test
    public void timeIntveralExceptionTestB() throws Exception{
        int threadCount = 30;
        LockSlideWindowRateLimiter rateLimiter = new LockSlideWindowRateLimiter(100, 5, 5, TimeUnit.SECONDS);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for(int j = 0; j < 5; ++j) {
            CountDownLatch countDownLatch = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; ++i) {
                executor.submit(new TestTask(rateLimiter, countDownLatch));
            }
            countDownLatch.await();
            Thread.sleep(2000);
        }
        Assert.assertEquals(1, rateLimiter.getInitializeCount());
        Assert.assertEquals(8, rateLimiter.getForwardCount());
        Assert.assertEquals(0, rateLimiter.getExceptionCount());
        Assert.assertEquals(90, rateLimiter.getCount());
    }

}
