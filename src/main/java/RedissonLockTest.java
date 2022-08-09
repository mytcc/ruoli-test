
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lock")
public class RedissonLockTest {

    public static int amount=5;

    @Autowired
    private RedissonClient redissonClient;

    //没获取到锁阻塞线程
    @RequestMapping(value="/test",method = RequestMethod.GET)
    public Integer test(){
        RLock lock=null;
        try {
            lock= redissonClient.getLock("lock");
            lock.lock();
            System.out.println(formatDate()+" "+Thread.currentThread().getName()+"获取到锁");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (null != lock&&lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return amount;
    }

    //立刻返回获取锁的状态
    @RequestMapping("/test1")
    public Integer test1(){
        RLock lock= null;
        try {
            lock= redissonClient.getLock("lock");
            if(lock.tryLock()) {
                System.out.println(formatDate() + " " + Thread.currentThread().getName() + "获取到锁");
                Thread.sleep(2000);
            }else{
                System.out.println(formatDate() + " " + Thread.currentThread().getName() + "已抢光");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (null != lock&&lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return amount;
    }

    //立刻返回获取锁的状态
    @RequestMapping("/test2")
    public Integer test2(){
        RLock lock= redissonClient.getLock("lock"); //非公平锁,随机取一个等待中的线程分配锁
        //RLock lock=redissonClient.getFairLock("lock"); //公平锁,按照先后顺序依次分配锁
        try {
            if (lock.tryLock(5, 4, TimeUnit.SECONDS)) { //最多等待锁2秒，10秒后强制解锁
                System.out.println(formatDate() + " " + Thread.currentThread().getName() + "获取到锁");
                Thread.sleep(4500);
            }else{
                System.out.println(formatDate() + " " + Thread.currentThread().getName() + "未获取到锁");
            }
        } catch(InterruptedException e){
            e.printStackTrace();
        } finally{
            if (null != lock && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return amount;
    }

    public String formatDate(){
        Calendar c=Calendar.getInstance();
        return c.get(Calendar.SECOND)+":"+c.get(Calendar.MILLISECOND);
    }
}
