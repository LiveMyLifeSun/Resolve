package Demo4;

/**
 * <p> 工厂的接口的 </p>
 *
 * @author Alemand
 * @since 2018/1/11
 */
public interface CarFactory {
    /**
     *创建汽车的方法
     *
     * @return 汽车的抽象类(在这里体现了设计原则)
     */
    Car createCar();
}
