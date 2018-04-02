package Demo7;

/**
 * <p> 人的具体的实现(ConcreteComponent) 实现Person </p>
 *
 * @author Alemand
 * @since 2018/1/25
 */
public class XiaoMing implements Person {

    @Override
    public void sing() {
        System.out.println("唱歌");
    }
}
