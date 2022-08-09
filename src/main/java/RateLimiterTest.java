import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* 限流器
* */
@RestController
@RequestMapping("/limiter")
public class RateLimiterTest {

    @Autowired
    private RedissonClient redissonClient;

    //初始化限流器
    @RequestMapping("/init")
    public void init(){
        RRateLimiter limiter=redissonClient.getRateLimiter("rateLimiter");
        limiter.trySetRate(RateType.PER_CLIENT,5,1, RateIntervalUnit.SECONDS);//每1秒产生5个令牌
    }

    //获取令牌
    @RequestMapping("/thread")
    public void thread(){
        RRateLimiter limiter=redissonClient.getRateLimiter("rateLimiter");
        if(limiter.tryAcquire()) {//尝试获取1个令牌
            System.out.println(Thread.currentThread().getName() + "成功获取到令牌");
        }else{
            System.out.println(Thread.currentThread().getName() + "未获取到令牌");
        }
    }
}
