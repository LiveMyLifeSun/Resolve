package Demo1;

import sun.security.provider.certpath.OCSP;

/**
 * <p> 具体的观察者,更据观察的结果去做一些事情 </p>
 *
 * @author Alemand
 * @since 2017/12/11
 */
public class OctalObserver extends Observer {

    public OctalObserver(Subject subject){
        this.subject = subject;
        this.subject.attach(this);
    }
    @Override
    public void update() {
        System.out.println("Octal String"+Integer.toOctalString(subject.getState()));
    }
}
