package Demo1.Demo2;

import java.util.Observable;
import java.util.Observer;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/11
 */
public class OctalObserver implements Observer {

    @Override
    public void update(Observable o, Object arg) {
        if(arg.toString().equals("duble clicked")){
            System.out.println("你对按钮执行了双击操做");
        }
    }
}
