package chatclient;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * CS262 Assignment 1
 */
public interface ClientCallback extends Remote{

    /**
     * Receives message from server and prints it to the console
     * @param message: message to receive
     * @throws RemoteException
     */
    void receiveMessage(String message) throws RemoteException;

}
