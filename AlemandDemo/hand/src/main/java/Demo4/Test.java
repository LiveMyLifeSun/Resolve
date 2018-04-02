package Demo4;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2018/1/11
 */
public class Test {
    public static void main(String[] args) {
        //这里也体现了设计原则
        CarFactory carFactory = new CarFactoryImpl();
        Car car = carFactory.createCar();
    }
}
