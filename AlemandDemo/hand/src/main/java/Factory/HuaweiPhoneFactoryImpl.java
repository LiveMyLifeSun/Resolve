package Factory;

/**
 * <p> 具体的手机工厂 </p>
 *
 * @author Alemand
 * @since 2018/1/17
 */
public class HuaweiPhoneFactoryImpl implements HuaweiFactory {

    /**
     * 创建手机的方法(里面所有的手机的具体对象构成手机产品族)
     *
     * @return 具体的手机
     */
    @Override
    public Phone createPhone(String phoneName) {
        if (phoneName.equals("mate9")) {
            return new Mate9();
        }
        if (phoneName.equals("mate10")) {
            return new Mate10();
        }
        return null;
    }

    @Override
    public Computer createComputer(String computerName) {
        return null;
    }
}
