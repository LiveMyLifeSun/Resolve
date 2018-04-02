package Demo;

/**
 * <p> 懒汉式的实现最终版 </p>
 *
 * @author Alemand
 * @since 2018/1/10
 */
public class Singleton {
    /**
     * 使用static修饰没有初始化并使用volatile修饰是变量可见
     */
    private volatile static Singleton singleton;
    /**
     * 单例设计模式的特点构造私有化
     */
    private Singleton() {};

    /**
     * 提供访问的接口返回该实例
     *
     * @return 该类的实例
     */
    public static  Singleton getSingleton() {
        //第一次判断为空来来确定是否进synchronized
     if (singleton == null){
        synchronized (Singleton.class){
            //第二次的判断来确定是否创建
            if (singleton == null){
                //创建实例对象
                singleton = new Singleton();
            }
        }
     }
        return singleton;
    }

}
