package Factory;

/**
 * <p> 具体的电脑工厂 </p>
 *
 * @author Alemand
 * @since 2018/1/19
 */
public class HuaweiComputerFactoryImpl implements HuaweiFactory {
    @Override
    public Phone createPhone(String phoneName) {
        return null;
    }

    /**
     * 生产电脑(里面所有的电脑的具体对象构成电脑产品族)
     *
     * @param computerName 电脑的名字
     * @return 电脑
     */
    @Override
    public Computer createComputer(String computerName) {
        if (computerName.equals("mateBook10")) {
            return new MateBook10();
        }
        if (computerName.equals("mateBook9")) {
            return new MateBook9();
        }
        return null;
    }
}
