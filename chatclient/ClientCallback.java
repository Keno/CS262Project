package chatclient;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Aaron on 2/28/2016.
 */
public interface ClientCallback extends Remote{

    void receiveMessage(String message) throws RemoteException;

}
