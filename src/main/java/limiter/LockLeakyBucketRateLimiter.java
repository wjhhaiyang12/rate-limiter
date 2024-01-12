package limiter;

import exception.RateLimiterException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//基于漏桶算法的限流工具
//使用锁实现线程安全
public class LockLeakyBucketRateLimiter implements RateLimiter{

    //最近一次请求的时间
    private AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());

    //桶的容量
    private final int capacity;

    //每毫秒消耗的容量
    private final double leakRateInMs;

    private AtomicInteger currentVolumn = new AtomicInteger(0);

    public LockLeakyBucketRateLimiter(int capacity, int leakRate, TimeUnit timeUnit){
        this.capacity = capacity;
        this.leakRateInMs = (double) leakRate / timeUnit.toMillis(1);
    }

    @Override
    public void execute() {
        //计算当前的容量
        long intervalTime = System.currentTimeMillis() - lastTime.get();
        currentVolumn.set(Math.max(0, currentVolumn.get() - (int)(intervalTime * leakRateInMs)));

        //判断限流
        if(currentVolumn.get() >= capacity){
            throw new RateLimiterException();
        }
        currentVolumn.incrementAndGet();
        lastTime.set(System.currentTimeMillis());
    }
}
