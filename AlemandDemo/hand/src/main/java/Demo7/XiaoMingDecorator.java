package Demo7;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * <p> 小明的装饰(ConcreteDecorator) </p>
 *
 * @author Alemand
 * @since 2018/1/25
 */
public class XiaoMingDecorator extends Decorator {

    public XiaoMingDecorator(Person person) {
        super(person);
    }

    @Override
    public void sing(){
        System.out.println("弹钢琴");
        super.sing();
    }

}
