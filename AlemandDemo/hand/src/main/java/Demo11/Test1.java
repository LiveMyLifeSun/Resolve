package Demo11;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/2/27
 */
public class Test1 {
    public static void main(String[] args) {
        //使用Executors创建线程池并规定
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println(1);
            }
        }, 10, TimeUnit.SECONDS);
      scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
          @Override
          public void run() {
              System.out.println(2);
          }
      },0,2,TimeUnit.SECONDS);
    }
}
