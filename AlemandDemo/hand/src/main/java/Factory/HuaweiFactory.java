package Factory;

/**
 * <p> 华为的抽象工厂 </p>
 *
 * @author Alemand
 * @since 2018/1/17
 */
public interface HuaweiFactory {
    
    /**
     * 生产手机
     *
     * @param phoneName 要创建什么手机的
     * @return 手机
     */
    Phone createPhone(String phoneName);

    /**
     * 生产电脑
     *
     * @param computerName 电脑的名字
     * @return 电脑
     */
    Computer createComputer(String computerName);

}
