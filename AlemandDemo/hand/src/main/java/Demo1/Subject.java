package Demo1;

import java.util.ArrayList;
import java.util.List;


/**
 * <p> 目标对象 </p>
 *
 * @author Alemand
 * @since 2017/12/11
 */
public class Subject {

    //可以观察我的对象
    private List<Observer> observers = new ArrayList<Observer>();

    private int state;

    public int getState(){
        return state;

    }

    //具体的观察的行为
    public void setState(int state){
        this.state = state;
        notifyALlObservers();
    }

    //限定可以观察我的对象并且将这个观察者传进来
    public void attach(Observer observer){
        observers.add(observer);
    }

    public void notifyALlObservers(){
        for (Observer observer :observers){
            observer.update();
        }
    }

}
