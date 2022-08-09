import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;
import org.redisson.api.ExpiredObjectListener;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/score")
public class RedisRScoredSortedSet {

    @Autowired
    private RedissonClient redissonClient;

    @RequestMapping("/add")
    public String addScore(String a,Double b){
        //创建Set
        RScoredSortedSet<String> set = redissonClient.getScoredSortedSet("simpleSet1");
        //设置过期时间
        boolean exists=set.isExists();
        set.addListener(new ExpiredObjectListener() {
            public void onExpired(String name) {
                System.out.println("超时事件被触发,name="+name);
                log.info("超时事件被触发,name={}",name);
            }
        });
        //添加元素
        set.addScore(a,b);
        if(!exists) {
            set.expireAt(DateUtils.addMinutes(new Date(), 2));
        }
        //获取元素在集合中的位置
        Integer index=set.revRank(a);
        //获取元素的评分
        Double score=set.getScore(a);
        log.info("size={},a={},index={},score={}",set.size(),a,index,score);

        //可以设置单一元属过期，但是不能触发对应过期事件
        RSetCache<String> map = redissonClient.getSetCache("simpleSet2");
        map.add(a,1, TimeUnit.MINUTES);
        map.addListener(new ExpiredObjectListener() {
            public void onExpired(String name) {
                log.info("entryExpiredListener超时事件被触发,event={}",name);
            }
        });

        //不能设置单一元属过期
        RSet<String> set1 = redissonClient.getSet("simpleSet3");
        set1.add(a);

        return "SUCCESS";
    }

    @RequestMapping("/show")
    public String showList(String key){
        log.info("排行榜={}", key);
        RScoredSortedSet<String> set = redissonClient.getScoredSortedSet(key);
        set.stream().forEach(a->{
            Integer index=set.revRank(a);//获取元素在集合中的位置
            Double score=set.getScore(a);//获取元素的评分
            log.info("size={},key={},index={},score={}", set.size(), a, index, score);
        });
        return "SUCCESS";
    }

    @RequestMapping("/clear")
    public String clearList(){
        long size=redissonClient.getKeys().deleteByPattern("*impl*");
        log.info("删除数量:{}",size);
        return "SUCCESS";
    }

    @RequestMapping("/deleteAll")
    public String deleteAll(String pattern){
        long amount=redissonClient.getKeys().deleteByPattern(pattern);
        log.info("删除数量:{}",amount);
        return "SUCCESS";
    }
}
