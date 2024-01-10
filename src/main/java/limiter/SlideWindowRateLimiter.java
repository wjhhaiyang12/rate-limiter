package limiter;

import exception.RateLimiterException;

import java.util.LinkedList;

//基于滑动窗口的计数器算法的限流工具，将窗口进一步分割成多个小窗口，根据时间的推移在多个小窗口上滑动，来提高限流的精确性
//非线程安全
public class SlideWindowRateLimiter {

    private final int threshold;

    private final int intervalSeconds;

    private final int blockNum;

    private final int blockSize;

    //记录每个小窗口的计数
    private final LinkedList<Integer> countList;

    private long startTime = 0;

    //总计数
    private int count = 0;

    public SlideWindowRateLimiter(int threshold, int intervalSeconds, int blockNum) {
        this.threshold = threshold;
        this.intervalSeconds = intervalSeconds;
        this.blockNum = blockNum;
        this.countList = new LinkedList<>();
        for(int i = 0; i < blockNum; ++i){
            this.countList.add(0);
        }
        this.blockSize = intervalSeconds * 1000 / blockNum;
    }

    public void execute(){
        if(threshold == 0) {
            throw new RateLimiterException();
        }

        //初始化
        if(startTime == 0){
            startTime = System.currentTimeMillis() - intervalSeconds * 1000L;
        }

        long intervalTime = System.currentTimeMillis() - startTime;
        int index = (int)intervalTime / blockSize;

        //前进到下一个时间窗口，丢弃前面窗口的计数
        if(index >= blockNum){
            for(int i = index; i >= blockNum; i--){
                forwardNextWindow();
            }
        }

        if(count >= threshold){
            throw new RateLimiterException();
        }
        countList.set(blockNum-1, countList.get(blockNum-1)+1);
        count++;
    }

    public void execute(int times){
        for(int i = 0; i < times; ++i){
            execute();
        }
    }

    private void forwardNextWindow(){
        int first = countList.peekFirst();
        countList.pollFirst();
        countList.addLast(0);
        count = count - first;
        startTime = startTime + blockSize;
    }

}
