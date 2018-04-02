package Demo4;

/**
 * <p> 工厂的实现类 </p>
 *
 * @author Alemand
 * @since 2018/1/11
 */
public class CarFactoryImpl implements CarFactory{
    /**
     *在子类实例化
     *
     * @return 汽车的抽象类(在这里体现了设计原则)
     */
    @Override
    public Car createCar() {
        Bwm bwm = new Bwm();
        bwm.color = "红色";
        bwm.type ="SUV";
        bwm.remind();
        return bwm;
    }
}
