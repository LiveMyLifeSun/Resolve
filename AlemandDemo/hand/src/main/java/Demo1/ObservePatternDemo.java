package Demo1;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/11
 */
public class ObservePatternDemo {
    public static void main(String[] args) {
        //创建目标对象
        Subject subject = new Subject();
        //将观察目标传到观察者的视野中,在这里也就是开始观察了
        new OctalObserver(subject);
        //当改变state时看观察者是否会做出变化
        subject.setState(10);
    }
}
