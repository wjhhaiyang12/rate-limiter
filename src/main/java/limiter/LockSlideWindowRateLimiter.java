package limiter;

import exception.RateLimiterException;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//基于滑动窗口的计数器算法的限流工具，将窗口进一步分割成多个小窗口，根据时间的推移在多个小窗口上滑动，来提高限流的精确性
//使用锁实现线程安全
public class LockSlideWindowRateLimiter implements RateLimiter{

    private final int threshold;

    private final int intervalSeconds;

    private final int blockNum;

    private final int blockSize;

    //记录每个小窗口的计数
    private final LinkedList<AtomicInteger> countList;

    private final AtomicLong startTime = new AtomicLong(0);

    //总计数
    private final AtomicInteger totalCount = new AtomicInteger(0);

    private final AtomicInteger initializeCount = new AtomicInteger(0);

    private final AtomicInteger forwardCount = new AtomicInteger(0);

    private final AtomicInteger exceptionCount = new AtomicInteger(0);

    public LockSlideWindowRateLimiter(int threshold, int intervalSeconds, int blockNum) {
        this.threshold = threshold;
        this.intervalSeconds = intervalSeconds;
        this.blockNum = blockNum;
        this.countList = new LinkedList<>();
        for(int i = 0; i < blockNum; ++i){
            this.countList.add(new AtomicInteger(0));
        }
        this.blockSize = intervalSeconds * 1000 / blockNum;
    }

    public void execute() {
        if (threshold == 0) {
            throw new RateLimiterException();
        }

        //初始化
        if(startTime.get() == 0L){
            initialize();
        }

        long intervalTime = System.currentTimeMillis() - startTime.get();
        int index = (int)intervalTime / blockSize;

        //前进到下一个时间窗口，丢弃前面窗口的计数
        if(index >= blockNum){
            forwardNextWindow();
        }

        //计数
        doExecute();
    }

    public void execute(int times){
        for(int i = 0; i < times; ++i){
            execute();
        }
    }

    public int getCount(){
        return totalCount.get();
    }

    public int getInitializeCount(){
        return initializeCount.get();
    }

    public int getForwardCount(){
        return forwardCount.get();
    }

    public int getExceptionCount(){
        return exceptionCount.get();
    }

    private synchronized void forwardNextWindow(){
        long intervalTime = System.currentTimeMillis() - startTime.get();
        int index = (int)intervalTime / blockSize;
        if(index >= blockNum) {
            for (int i = index; i >= blockNum; i--) {
                forwardCount.incrementAndGet();
                int first = countList.peekFirst().get();
                countList.pollFirst();
                countList.addLast(new AtomicInteger(0));
                totalCount.set(totalCount.get() - first);
                startTime.set(startTime.get() + blockSize);
            }
        }
    }

    private synchronized void doExecute(){
        if(totalCount.get() >= threshold){
            exceptionCount.incrementAndGet();
            throw new RateLimiterException();
        }
        countList.peekLast().incrementAndGet();
        totalCount.incrementAndGet();
    }

    private synchronized void initialize(){
        if(startTime.get() == 0L){
            initializeCount.incrementAndGet();
            startTime.set(System.currentTimeMillis() - (long) (blockNum - 1) * blockSize);
        }
    }
}
