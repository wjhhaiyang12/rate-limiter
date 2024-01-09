package limiter;

import exception.RateLimiterException;
import org.junit.Assert;
import org.junit.Test;

public class CountRateLimiterTest {

    @Test
    public void normalTest(){
        CountRateLimiter rateLimiter = new CountRateLimiter(10, 1);
        rateLimiter.execute(10);
    }

    @Test
    public void overCountTest(){
        try {
            CountRateLimiter rateLimiter = new CountRateLimiter(10, 1);
            rateLimiter.execute(11);
        } catch (Exception e) {
            Assert.assertEquals(RateLimiterException.class.getName(), e.getClass().getName());
        }
    }

    @Test
    public void timeWindowNormalTest() throws Exception{
        CountRateLimiter rateLimiter = new CountRateLimiter(10, 1);
        for(int i = 0; i < 5; ++i) {
            rateLimiter.execute(10);
            Thread.sleep(1000);
        }
    }

    @Test
    public void timeWindowOverCountTest(){
        try {
            CountRateLimiter rateLimiter = new CountRateLimiter(10, 1);
            rateLimiter.execute(10);
            Thread.sleep(500);
            rateLimiter.execute(1);
        } catch (Exception e) {
            Assert.assertEquals(RateLimiterException.class.getName(), e.getClass().getName());
        }
    }


}
