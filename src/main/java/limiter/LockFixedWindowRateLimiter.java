package limiter;

import exception.RateLimiterException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//基于固定窗口的计数器算法的限流工具，比如限制一秒内最多调用50次
//使用锁实现线程安全
public class LockFixedWindowRateLimiter implements RateLimiter{

    private final int threshold;

    private final long windowSize;

    private AtomicInteger count = new AtomicInteger(0);

    private volatile long pointTime;

    private final AtomicInteger initializeCount = new AtomicInteger(0);

    private final AtomicInteger refreshCount = new AtomicInteger(0);

    private final AtomicInteger exceptionCount = new AtomicInteger(0);

    public LockFixedWindowRateLimiter(int threshold, int windowSize, TimeUnit timeUnit) {
        this.threshold = threshold;
        this.windowSize = timeUnit.toMillis(windowSize);
    }

    public void execute(){
        //阈值为零，禁止执行
        if(threshold == 0){
            throw new RateLimiterException();
        }

        //首次初始化
        if(pointTime == 0){
            if(initalize()){
                return;
            }
        }

        long currentTime = System.currentTimeMillis();
        long intervalTime = currentTime - pointTime;

        //在上一个窗口里
        if(intervalTime < windowSize){
            doExecute();
            return;
        }

        //新窗口，刷新时间和计数
        if(!refresh(pointTime)){
            doExecute();
        }
    }

    public void execute(int times){
        for(int i = 0; i < times; ++i){
            execute();
        }
    }

    public int getCount(){
        return count.get();
    }

    public int getInitializeCount(){
        return initializeCount.get();
    }

    public int getRefreshCount(){
        return refreshCount.get();
    }

    public int getExceptionCount(){
        return exceptionCount.get();
    }


    private synchronized boolean initalize(){
        if(pointTime == 0) {
            initializeCount.incrementAndGet();
            pointTime = System.currentTimeMillis();
            count.set(1);
            return true;
        }
        return false;
    }

    private synchronized boolean refresh(long oldPointTime){
        if(pointTime == oldPointTime) {
            refreshCount.incrementAndGet();
            pointTime = System.currentTimeMillis();
            count.set(1);
            return true;
        }
        return false;
    }

    private synchronized void doExecute(){
        if(count.get() >= threshold){
            exceptionCount.incrementAndGet();
            throw new RateLimiterException();
        }
        count.incrementAndGet();
    }
}
