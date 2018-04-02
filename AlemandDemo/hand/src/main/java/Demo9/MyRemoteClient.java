package Demo9;

import java.rmi.Naming;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2018/2/5
 */
public class MyRemoteClient {
    public static void main(String[] args) {
        try {
            MyRemoteService myRemoteService = (MyRemoteService) Naming.lookup("rmi://127.0.0.1/RemoteHello");
            myRemoteService.sayHello();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
