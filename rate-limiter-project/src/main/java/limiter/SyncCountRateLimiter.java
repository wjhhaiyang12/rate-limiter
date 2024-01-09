package limiter;

import exception.RateLimiterException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

//基于计数器算法的限流工具，比如限制一秒内最多调用50次
//线程安全 + 锁
public class SyncCountRateLimiter {

    private final int threshold;

    private final int intervalSeconds;

    private AtomicInteger count = new AtomicInteger(0);

    private volatile long pointTime;

    private final ReentrantLock initLock = new ReentrantLock();

    private final ReentrantLock countLock = new ReentrantLock();

    private final ReentrantLock refreshLock = new ReentrantLock();

    public SyncCountRateLimiter(int threshold, int intervalSeconds) {
        this.threshold = threshold;
        this.intervalSeconds = intervalSeconds;
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
        if(intervalTime < intervalSeconds * 1000L){
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

    private boolean initalize(){
        initLock.lock();
        if(pointTime == 0) {
            pointTime = System.currentTimeMillis();
            count.incrementAndGet();
            initLock.unlock();
            return true;
        }
        initLock.unlock();
        return false;
    }

    private boolean refresh(long oldPointTime){
        refreshLock.lock();
        if(pointTime == oldPointTime) {
            pointTime = System.currentTimeMillis();
            count.set(1);
            refreshLock.unlock();
            return true;
        }
        refreshLock.unlock();
        return false;
    }

    private void doExecute(){
        countLock.lock();
        if(count.get() >= threshold){
            countLock.unlock();
            throw new RateLimiterException();
        }
        count.incrementAndGet();
        countLock.unlock();
    }
}
