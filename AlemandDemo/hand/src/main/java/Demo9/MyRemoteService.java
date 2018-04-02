package Demo9;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2018/2/5
 */
public interface MyRemoteService extends Remote {

    /**
     * 说hello world
     *
     * @throws RemoteException 调用异常
     */
    void sayHello() throws RemoteException;
}
