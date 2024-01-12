package limiter;

import exception.RateLimiterException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class FixedWindowRateLimiterTest {

    @Test
    public void normalTest(){
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(10, 1, TimeUnit.SECONDS);
        rateLimiter.execute(10);
    }

    @Test
    public void overCountTest(){
        try {
            FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(10, 1, TimeUnit.SECONDS);
            rateLimiter.execute(11);
        } catch (Exception e) {
            Assert.assertEquals(RateLimiterException.class.getName(), e.getClass().getName());
        }
    }

    @Test
    public void timeWindowNormalTest() throws Exception{
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(10, 1, TimeUnit.SECONDS);
        for(int i = 0; i < 5; ++i) {
            rateLimiter.execute(10);
            Thread.sleep(1000);
        }
    }

    @Test
    public void timeWindowOverCountTest(){
        try {
            FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(10, 1, TimeUnit.SECONDS);
            rateLimiter.execute(10);
            Thread.sleep(500);
            rateLimiter.execute(1);
        } catch (Exception e) {
            Assert.assertEquals(RateLimiterException.class.getName(), e.getClass().getName());
        }
    }


}
