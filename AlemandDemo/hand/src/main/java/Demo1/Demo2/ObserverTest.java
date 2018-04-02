package Demo1.Demo2;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/11
 */
public class ObserverTest {
    public static void main(String[] args) {
        Subject subject = new Subject();
        OctalObserver octalObserver = new OctalObserver();
        subject.addObserver(octalObserver);
        subject.setClickMode("duble clicked");

    }
}
