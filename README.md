# 单机限流器

## 功能

提供基于多种限流算法的单机限流器。 每一种限流器都提供了线程安全和非线程的版本，其中线程安全的版本提供了基于锁的，以及基于无锁CAS操作的两个版本。
- 固定窗口
- 滑动窗口
- 漏桶
- 令牌桶
- ...

## 使用

### 固定窗口限流器
固定时间窗口，每个窗口里只允许指定的流量，到新的时间窗口后重新计数。固定窗口限流不够精确，在两个窗口交接处，可以允许出现两倍的限流流量。
- FixedWindowRateLimiter：基于固定窗口的限流器，非线程安全
- LockFixedWindowRateLimiter：基于固定窗口的限流器，使用锁实现线程安全
- CasFixedWindowRateLimiter：基于固定窗口的限流器，使用CAS实现线程安全
```
int threshold = 10; //限流次数
int windowSize = 1; //固定窗口大小
TimeUnit timeUnit = TimeUnit.SECONDS; //窗口时间单位
FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(threshold, windowSize, timeUnit);
rateLimiter.execute(10);
```

### 滑动窗口限流器
在固定窗口的基础上，将窗口进一步切分成多个小窗口，来提高限流的精确性。
- SlideWindowRateLimiter：基于滑动窗口的限流器，非线程安全
- LockSlideWindowRateLimiter：基于滑动窗口的限流器，使用锁实现线程安全
- CasSlideWindowRateLimiter：基于滑动窗口的限流器，使用CAS实现线程安全
```
int threshold = 100; //限流次数
int intervalSeconds = 10; //固定窗口大小
int blockNum = 10; //窗口分片数量，分片越多，限流越精准
TimeUnit timeUnit = TimeUnit.SECONDS; //窗口时间单位
SlideWindowRateLimiter rateLimiter = new SlideWindowRateLimiter(threshold, intervalSeconds, blockNum, timeUnit);
rateLimiter.execute(10);
```

### 漏桶限流器
- LeakyBucketRateLimiter: 基于漏桶算法的限流器，非线程安全
- LockLeakyBucketRateLimiter：基于漏桶算法的限流器，使用锁实现线程安全
- CasLeakyBucketRateLimiter：基于漏桶算法的限流器，使用CAS实现线程安全

## 联系我
目前还是一个写着玩玩的玩具项目，后续会补充更多的限流算法的实现，调研我的实现和guava实现的区别，做一些性能的benchmark，未来也可能会扩展分布式限流的功能，有任何问题可以联系微信13681688983
