package Demo11;

import java.util.Timer;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/2/26
 */
public class Test {
    public static void main(String[] args) {
        //创建Timer对象
        Timer timer = new Timer();
        //获取当前设备的cpu
        int i = Runtime.getRuntime().availableProcessors();
        //使用方法schedule将timerTask的实现作为参数传出方法中
        //schedule方法有许多重载的方法
        //这个方法是重现在开始延迟10秒执行
        //timer.schedule(new TimeTest(1),10*1000);
        //这个方法是重现在开始延迟20秒执行以后每10秒执行一次
        //timer.schedule(new TimeTest(2),20*1000,10*1000);
    }
}
