package Demo;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/8
 */
public class Demo5 {

    public static void main(String[] args) {
       //创建线程池的五种方式
        //1.单例线程,表示在任意时间段内,线程池中只有一个线程在工作
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        //2.缓存线程池,线程池中是否有当前执行线程的缓存.如果有就复用,如果没有那么需要创建一个线程
        //来完成当前的调用,并且这类线程池只能完成当前的调用,并且这类线程池只能完成一些生存期很短的一些任务
        //,并且这类线程池内部规定能复用的线程,空闲的时间不能超过30s,一旦超过60s,就会被移除线程池.
        ExecutorService executorService1 = Executors.newCachedThreadPool();
        //3.固定线程池,和newCacheThreadPool()差不多,也能实现复用,但是这个池子规定了线程的最大数量,也就是说
        //当池子有空闲时,那么新的任务将会在空闲线程中被执行,一旦线程池内的线程都在进行工作,那么新的任务就必须等待
        //线程池用空闲的时候才能进入线程池,其他的任务继续排队等待.这类池子没有规定空闲的时间到底有多长,这一类的池子
        //更适用于服务器
        ExecutorService executorService2 = Executors.newFixedThreadPool(10);
        //4.调度型线程池,调度型线程池会根据Scheduled(任务列表)进行延迟执行,或者是进行周期性的的执行,适用一些周期性的工作任务
        ScheduledExecutorService executorService3 = Executors.newScheduledThreadPool(10);
        //创建任务
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while(true){
                    System.out.println("hello world");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //创建任务分为两种:一种是有返回值的callable,一种是没有返回值的runnable,callable于Future两个功能是Java在后续
        //的版本中为了适应多并发才加进去的,Callable是类似于Runnable的接口,实现Callable接口的类都是可被其他线程执行的任务
        //无返回值的任务就是实现了Runnable接口的类,使用run方法
        //有返回值的任务是实现了callable接口得类,使用call方法
        Future<Object> submit = executorService1.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return "nihao";
            }
        });

        //Future的介绍


    }
}
