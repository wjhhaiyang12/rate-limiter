package limiter;

import exception.RateLimiterException;
import org.junit.Assert;
import org.junit.Test;

public class SlideWindowRateLimiterTest {

    @Test
    public void normalTest(){
        SlideWindowRateLimiter rateLimiter = new SlideWindowRateLimiter(100, 5, 5);
        rateLimiter.execute(100);
        System.out.println("dd");
    }

    @Test
    public void exceptionTest(){
        try {
            SlideWindowRateLimiter rateLimiter = new SlideWindowRateLimiter(100, 5, 5);
            rateLimiter.execute(101);
        } catch (Exception e) {
            Assert.assertEquals(RateLimiterException.class.getName(), e.getClass().getName());
        }
    }

    @Test
    public void intervalTest() throws Exception{
        SlideWindowRateLimiter rateLimiter = new SlideWindowRateLimiter(100, 5, 5);
        rateLimiter.execute(100);
        Thread.sleep(5000);
        rateLimiter.execute(100);
    }

    @Test
    public void intervalExceptionTest() throws Exception{
        SlideWindowRateLimiter rateLimiter = new SlideWindowRateLimiter(100, 5, 5);
        rateLimiter.execute(100);
        Thread.sleep(3000);
        try {
            rateLimiter.execute(100);
        } catch (Exception e) {
            Assert.assertEquals(RateLimiterException.class.getName(), e.getClass().getName());
        }
    }

    @Test
    public void fastExceptionTest() throws Exception{
        SlideWindowRateLimiter rateLimiter = new SlideWindowRateLimiter(100, 5, 5);
        rateLimiter.execute(1);
        Thread.sleep(4000);
        rateLimiter.execute(99);
        Thread.sleep(1000);
        try {
            rateLimiter.execute(100);
        } catch (Exception e) {
            Assert.assertEquals(RateLimiterException.class.getName(), e.getClass().getName());
        }
    }

}
