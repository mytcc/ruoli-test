import javax.annotation.PostConstruct;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* 话题(订阅分发)
* */
@RestController
@RequestMapping("/topic")
public class RedisTopicTest {

    @Autowired
    private RedissonClient redissonClient;

    //分发
    @RequestMapping("/produce")
    public String produce(String a){
        RTopic topic = redissonClient.getTopic("anyTopic");
        topic.publish(a);
        return "发送消息:"+a;
    }

    //订阅
    @PostConstruct
    public void consume() {
        RTopic topic=redissonClient.getTopic("anyTopic");//订阅指定话题
        //RPatternTopic topic=redissonClient.getPatternTopic("*any*");//指定话题表达式订阅多个话题
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String map) {
                System.out.println("接收到消息:"+map);
            }
        });
    }
}
