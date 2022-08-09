import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.redisson.api.RMap;
import org.redisson.api.RTransaction;
import org.redisson.api.RedissonClient;
import org.redisson.api.TransactionOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* Redisson为RMap、RMapCache、RLocalCachedMap、RSet、RSetCache和RBucket这样的对象提供了具有ACID属性的事务功能
* Redisson事务通过分布式锁保证了连续写入的原子性，同时在内部通过操作指令队列实现了Redis原本没有的提交与滚回功能
* 当提交与滚回遇到问题的时候，将通过org.redisson.transaction.TransactionException告知用户
* */
@RestController
@RequestMapping("/tx")
public class RedisTransactionTest {

    @Autowired
    private RedissonClient redissonClient;

    @RequestMapping("/test/{key}")
    public void test(@PathVariable String key){
        TransactionOptions options=TransactionOptions.defaults().syncSlavesTimeout(5, TimeUnit.SECONDS)
                .responseTimeout(3,TimeUnit.SECONDS).retryInterval(2,TimeUnit.SECONDS)
                .retryAttempts(3).timeout(5,TimeUnit.SECONDS);
        RTransaction transaction=redissonClient.createTransaction(options);
        RMap<String, Integer> map=transaction.getMap("myMap");
        System.out.println(map.get("userId"));
        map.put("userId", RandomUtils.nextInt(1,100));
        System.out.println(map.get(key).toString());
        try {
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            transaction.rollback();
        }
    }
}
