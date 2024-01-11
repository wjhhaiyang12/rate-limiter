package limiter;

import exception.RateLimiterException;

//基于固定窗口的计数器算法的限流工具，比如限制一秒内最多调用50次
//非线程安全
public class FixedWindowRateLimiter implements RateLimiter{

    private final int threshold;

    private final int intervalSeconds;

    private int count;

    private long pointTime;

    public FixedWindowRateLimiter(int threshold, int intervalSeconds) {
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
            pointTime = System.currentTimeMillis();
            count++;
            return;
        }

        long currentTime = System.currentTimeMillis();
        long intervalTime = currentTime - pointTime;
        if(intervalTime < intervalSeconds * 1000L){
            //在上一个窗口里
            if(count >= threshold){
                throw new RateLimiterException();
            }
            count++;
        } else {
            //新的窗口
            pointTime = System.currentTimeMillis();
            count = 1;
        }
    }

    public void execute(int times){
        for(int i = 0; i < times; ++i){
            execute();
        }
    }

}
