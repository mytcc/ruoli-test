import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryExpiredListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/map")
public class RedissonMapTest {

    @Autowired
    private RedissonClient redissonClient;
    private final static String key="my_test_map";

    //初始化Listener，仅初始化一次，过期事件不一定那么及时触发，存在一定的延时
    @PostConstruct
    public void init(){
        RMapCache<String, String> map=redissonClient.getMapCache(key);
        map.addListener(new EntryExpiredListener<String, String>() {
            @Override
            public void onExpired(EntryEvent<String, String> event) {
                log.info("{}已过期,原来的值为:{},现在的值为:{}",event.getKey(),event.getOldValue(),event.getValue());
            }
        });
        log.info("{}初始化完成",key);
    }

    //存放Key-Value对
    @RequestMapping("/put")
    public String put(String a,String b,boolean flag){
        RMapCache<String, String> map=redissonClient.getMapCache(key);
        if(flag) {
            map.put(a, b, 10, TimeUnit.SECONDS);
        }else{
            map.put(a, b);
        }
        log.info("设置{}={}成功",a,b);
        return "SUCCESS";
    }

    @RequestMapping("/show")
    public String put(){
        RMapCache<String, String> map=redissonClient.getMapCache(key);
        map.keySet().stream().forEach(i->log.info("{},{}",i,map.get(i)));
        return "SUCCESS";
    }
}
