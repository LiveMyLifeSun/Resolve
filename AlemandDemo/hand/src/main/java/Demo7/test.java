package Demo7;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2018/1/25
 */
public class test {
    public static void main(String[] args) {
        //将小明包装了
        XiaoMingDecorator xiaoMingDecorator = new XiaoMingDecorator(new XiaoMing());
        //看到上面的对象创建的方式是不是有点像 new BufferedInputStream(new FileInputStream());
        //其实在java中IO就用到了这种模式
        xiaoMingDecorator.sing();
    }
}
