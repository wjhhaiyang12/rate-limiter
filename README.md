# 单机限流器

## 功能

提供基于多种限流算法的单机限流器。 每一种限流器都提供了线程安全和非线程的版本，其中线程安全的版本提供了基于锁的，以及基于CAS操作的两个版本。
- 固定窗口
- 滑动窗窗口
- 令牌桶
- ...

## 使用

- FixedWindowRateLimiter：基于固定窗口的限流器，非线程安全
- LockFixedWindowRateLimiter：基于固定窗口的限流器，使用锁实现线程安全
- CasFixedWindowRateLimiter：基于固定窗口的限流器，使用CAS实现线程安全
```
int threshold = 10; //限流次数
int intervalSeconds = 1; //固定窗口大小
FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(threshold, intervalSeconds);
rateLimiter.execute(10);
```

- SlideWindowRateLimiter：基于滑动窗口的限流器，非线程安全
```
int threshold = 100; //限流次数
int intervalSeconds = 10; //固定窗口大小
int blockNum = 10; //窗口分片数量，分片越多，限流越精准
SlideWindowRateLimiter rateLimiter = new SlideWindowRateLimiter(threshold, intervalSeconds, blockNum);
rateLimiter.execute(10);
```

## 联系我
目前还是一个写着玩玩的玩具项目，感兴趣的可以联系微信13681688983