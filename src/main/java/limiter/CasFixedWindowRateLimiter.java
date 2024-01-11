package limiter;

import exception.RateLimiterException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//基于固定窗口的计数器算法的限流工具，比如限制一秒内最多调用50次
//使用CAS操作实现线程安全
public class CasFixedWindowRateLimiter implements RateLimiter{

    private final int threshold;

    private final int intervalSeconds;

    private final AtomicInteger count = new AtomicInteger(0);

    private final AtomicLong pointTime = new AtomicLong(0);

    private final AtomicInteger initializeCount = new AtomicInteger(0);

    private final AtomicInteger refreshCount = new AtomicInteger(0);

    private final AtomicInteger exceptionCount = new AtomicInteger(0);

    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    public CasFixedWindowRateLimiter(int threshold, int intervalSeconds) {
        this.threshold = threshold;
        this.intervalSeconds = intervalSeconds;
    }

    public void execute(){
        //阈值为零，禁止执行
        if(threshold == 0){
            throw new RateLimiterException();
        }

        //首次初始化
        if(pointTime.get() == 0){
            if(initalize()){
                return;
            }
        }

        int tempRefreshCount = refreshCount.get();
        long currentTime = System.currentTimeMillis();
        long intervalTime = currentTime - pointTime.get();

        //在上一个窗口里
        if(intervalTime < intervalSeconds * 1000L){
            doExecute();
            return;
        }

        //新窗口，刷新时间和计数
        refresh(tempRefreshCount);

        //计数
        doExecute();
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


    private boolean initalize(){
        while (pointTime.get() == 0){
            if(count.compareAndSet(0, 1)){
                initializeCount.incrementAndGet();
                pointTime.set(System.currentTimeMillis());
                return true;
            }
        }
        return false;
    }

    private void refresh(int tempRefreshCount){
        while (tempRefreshCount == refreshCount.get()) {
            if(refreshing.compareAndSet(false, true)){
                if(tempRefreshCount != refreshCount.get()){
                    refreshing.set(false);
                    continue;
                }
                pointTime.set(System.currentTimeMillis());
                refreshCount.incrementAndGet();
                count.set(0);
                refreshing.set(false);
            }
        }
    }

    private void doExecute(){
        while (true) {
            if(refreshing.get()){
                continue;
            }

            if(count.get() >= threshold){
                exceptionCount.incrementAndGet();
                throw new RateLimiterException();
            }

            int currentCount = count.get();
            if(count.compareAndSet(currentCount, currentCount+1)){
                break;
            }
        }
    }
}
