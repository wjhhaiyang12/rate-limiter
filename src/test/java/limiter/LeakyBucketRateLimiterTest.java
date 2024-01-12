package limiter;

import exception.RateLimiterException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class LeakyBucketRateLimiterTest {

    @Test
    public void normalTest(){
        LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter(100, 5, TimeUnit.SECONDS);
        rateLimiter.execute(100);
    }

    @Test
    public void exceptionTest(){
        try {
            LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter(100, 5, TimeUnit.SECONDS);
            rateLimiter.execute(101);
        } catch (Exception e) {
            Assert.assertEquals(RateLimiterException.class.getName(), e.getClass().getName());
        }
    }

    @Test
    public void continuousTest() throws Exception{
        LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter(100, 5, TimeUnit.SECONDS);
        for(int i = 0; i < 6; ++i) {
            rateLimiter.execute(20);
            Thread.sleep(1000);
        }
    }

    @Test
    public void continuousExceptionTest() {
        int second = 0;
        try {
            LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter(100, 5, TimeUnit.SECONDS);
            for (int i = 0; i < 10; ++i) {
                second++;
                rateLimiter.execute(20);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Assert.assertEquals(RateLimiterException.class.getName(), e.getClass().getName());
            Assert.assertEquals(7, second);
        }
    }

}
