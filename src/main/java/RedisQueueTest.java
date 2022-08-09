import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RList;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//List、Queue测试
@RestController
@RequestMapping("/collection")
public class RedisQueueTest {

  @Autowired
  private RedissonClient redissonClient;

  //List测试 - 添加元素
  @RequestMapping("/list/add")
  public List<String> addAndGetList(String a){
      RList<String> list = redissonClient.getList("my_list");
      list.add(a);
      return list.readAll();
  }

  //List测试 - 删除元素
  @RequestMapping("/list/del")
  public List<String> removeList(String a){
      RList<String> list = redissonClient.getList("my_list");
      /* 自定义删除条件
      list.removeIf(new Predicate<String>() {
          @Override
          public boolean test(String s) {
              return s.length()>10;
          }
      });*/

      //list.remove(a);//删除元素,仅删除匹配到的第一个元素
      list.removeAll(Arrays.asList(a));//删除指定集合中所有元素
      return list.readAll();
  }

  //Queue测试 - 添加元素
  @RequestMapping("/queue/add")
  public List<String> addQueue(String a){
      RQueue<String> list = redissonClient.getQueue("my_queue");
      list.add(a);//添加一个元素到集合最末尾
      return list.readAll();
  }

  //Queue测试 - 读取元素
  @RequestMapping("/queue/poll")
  public String pollQueue(){
      RQueue<String> list = redissonClient.getQueue("my_queue");
      return list.poll();//从队列的头部获取一个元素并从队列中删除该元素，队列为空时返回null
  }

  //Blocking Queue测试 - 添加元素
  @RequestMapping("/blocking/add")
  public List<String> addBlockingQueue(String a){
      RBlockingQueue<String> list = redissonClient.getBlockingQueue("my_blocking_queue");
      list.add(a);
      return list.readAll();
  }

  //Blocking Queue测试 - 读取元素
  @RequestMapping("/blocking/get")
  public String getBlockingQueue() throws InterruptedException {
      RBlockingQueue<String> list = redissonClient.getBlockingQueue("my_blocking_queue");
      //return list.poll();//从队列的头部获取一个元素并从队列中删除该元素，队列为空时返回null
      return list.take();//从队列的头部获取一个元素并从队列中删除该元素，队列为空时阻塞线程
      //return list.peek();//从队列的头部获取一个元素但不删除该元素，队列为空时返回null
  }

  //Delayed Queue测试 - 添加元素
  @RequestMapping("/delayed/add")
  public List<String> addDelayedQueue(String a,Long b){
      RQueue<String> queue = redissonClient.getQueue("my_blocking_queue");//目标队列
      RDelayedQueue<String> list = redissonClient.getDelayedQueue(queue);//延迟队列，数据临时存放地，发出后删除该元素
      list.offer(a, b, TimeUnit.SECONDS);
      return list.readAll();
  }

  @PostConstruct
  public void acceptElement(){
      RBlockingQueue<String> list = redissonClient.getBlockingQueue("my_blocking_queue");
      list.subscribeOnElements(new Consumer<String>() {
          public void accept(String s) {
              System.out.println("获取到元素:"+s);
          }
      });
  }
}
