package limiter;

import exception.RateLimiterException;

import java.util.concurrent.TimeUnit;

//基于漏桶算法的限流工具
//非线程安全
public class LeakyBucketRateLimiter implements RateLimiter {

    //最近一次请求的时间
    private long lastTime = System.currentTimeMillis();

    //桶的容量
    private final int capacity;

    //每毫秒消耗的容量
    private final double leakRateInMs;

    private int currentVolumn = 0;

    public LeakyBucketRateLimiter(int capacity, int leakRate, TimeUnit timeUnit){
        this.capacity = capacity;
        this.leakRateInMs = (double) leakRate / timeUnit.toMillis(1);
    }

    @Override
    public void execute() {
        //计算当前的容量
        long intervalTime = System.currentTimeMillis() - lastTime;
        currentVolumn = Math.max(0, currentVolumn - (int)(intervalTime * leakRateInMs));

        //判断限流
        if(currentVolumn >= capacity){
            throw new RateLimiterException();
        }
        currentVolumn++;
        lastTime = System.currentTimeMillis();
    }

    public void execute(int times) {
        for(int i = 0; i < times; ++i){
            execute();
        }
    }
}
