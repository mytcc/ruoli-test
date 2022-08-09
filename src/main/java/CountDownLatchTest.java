import java.util.concurrent.TimeUnit;

import org.redisson.api.RCountDownLatch;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/count")
public class CountDownLatchTest {

    @Autowired
    private RedissonClient redissonClient;

    //主线程等待所有子线程完成
    @RequestMapping("/await")
    public void await(){
        try {
            RCountDownLatch latch = redissonClient.getCountDownLatch("latch");
            latch.trySetCount(3);//设置计数器初始大小
            latch.await();//阻塞线程直到计数器归零
            System.out.println(Thread.currentThread().getName()+"所有子线程已运行完毕");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //子线程
    @RequestMapping("/thread")
    public void thread(){
        try {
            RCountDownLatch latch = redissonClient.getCountDownLatch("latch");
            System.out.println(Thread.currentThread().getName()+"抵达现场");
            TimeUnit.SECONDS.sleep(5);
            latch.countDown();//计数器减1，当计数器归零后通知所有等待着的线程恢复执行
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
