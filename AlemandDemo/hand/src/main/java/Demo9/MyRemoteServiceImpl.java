package Demo9;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2018/2/5
 */
public class MyRemoteServiceImpl extends UnicastRemoteObject implements MyRemoteService {

    public MyRemoteServiceImpl() throws RemoteException {

    }

    @Override
    public void sayHello() throws RemoteException {
        System.out.println("hello world");
    }

    public static void main(String[] args) {
        try {
            MyRemoteService myRemoteService = new MyRemoteServiceImpl();
            Naming.bind("rmi://127.0.0.1:1096/RemoteHello", myRemoteService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
