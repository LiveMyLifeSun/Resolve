package Demo1.Demo2;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * <p> 观察目标 </p>
 *
 * @author Alemand
 * @since 2017/12/11
 */
public class Subject extends Observable {
    private String clickMode;

    public String getClickMode(){
        return clickMode;
    }

    public void setClickMode(String clickMode){
        this.clickMode=clickMode;
        this.setChanged();
        this.notifyObservers(clickMode);
    }




}
