package limiter;

import exception.RateLimiterException;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//基于滑动窗口的计数器算法的限流工具，将窗口进一步分割成多个小窗口，根据时间的推移在多个小窗口上滑动，来提高限流的精确性
//使用CAS操作实现线程安全
public class CasSlideWindowRateLimiter implements RateLimiter{

    private final int threshold;

    private final long windowSize;

    private final int blockNum;

    private final long blockSize;

    //记录每个小窗口的计数
    private final LinkedList<AtomicInteger> countList;

    private final AtomicLong startTime = new AtomicLong(0);

    //总计数
    private final AtomicInteger totalCount = new AtomicInteger(0);

    private final AtomicInteger initializeCount = new AtomicInteger(0);

    private final AtomicInteger forwardCount = new AtomicInteger(0);

    private final AtomicInteger exceptionCount = new AtomicInteger(0);

    private final AtomicBoolean forwarding = new AtomicBoolean(false);

    public CasSlideWindowRateLimiter(int threshold, int windowSize, int blockNum, TimeUnit timeUnit) {
        this.threshold = threshold;
        this.windowSize = timeUnit.toMillis(windowSize);
        this.blockNum = blockNum;
        this.countList = new LinkedList<>();
        for(int i = 0; i < blockNum; ++i){
            this.countList.add(new AtomicInteger(0));
        }
        this.blockSize = this.windowSize / blockNum;
    }

    @Override
    public void execute() {
        if (threshold == 0) {
            throw new RateLimiterException();
        }

        //初始化
        if(startTime.get() == 0L){
            initialize();
        }

        int tempForwardCount = forwardCount.get();
        long intervalTime = System.currentTimeMillis() - startTime.get();
        int index = (int) (intervalTime / blockSize);

        //前进到下一个时间窗口，丢弃前面窗口的计数
        if(index >= blockNum){
            forwardNextWindow(tempForwardCount, index);
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

    private void forwardNextWindow(int tempForwardCount, int index){
        while (tempForwardCount == forwardCount.get()) {
            if(forwarding.compareAndSet(false, true)) {
                if(tempForwardCount != forwardCount.get()){
                    forwarding.set(false);
                    continue;
                }

                for (int i = index; i >= blockNum; i--) {
                    int first = countList.peekFirst().get();
                    countList.pollFirst();
                    countList.addLast(new AtomicInteger(0));
                    totalCount.set(totalCount.get() - first);
                    startTime.set(startTime.get() + blockSize);
                    forwardCount.incrementAndGet();
                }
                forwarding.set(false);
            }
        }
    }

    private void doExecute(){
        while (true) {
            if(forwarding.get()){
                continue;
            }

            if (totalCount.get() >= threshold) {
                exceptionCount.incrementAndGet();
                throw new RateLimiterException();
            }

            int tempCount = totalCount.get();
            if(totalCount.compareAndSet(tempCount, tempCount+1)){
                countList.peekLast().incrementAndGet();
                break;
            }
        }
    }

    private void initialize(){
        while(startTime.get() == 0){
            if(startTime.compareAndSet(0, System.currentTimeMillis() - (long) (blockNum - 1) * blockSize)){
                initializeCount.incrementAndGet();
            }
        }
    }
}
