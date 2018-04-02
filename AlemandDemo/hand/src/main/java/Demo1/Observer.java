package Demo1;

/**
 * <p> 观察者的抽象类如果继承了该类就相当于有了观察者的能力了</p>
 *
 * @author Alemand
 * @since 2017/12/11
 */
public abstract class Observer {
    //要观察的对象
    protected Subject subject;
    //观察后要做的事情
    public abstract void update();
}
