/**
 * CS262 Assignment 1
 * References: Remote Method Invocation and Object Serialization reading from class
 *             Oracle Tutoral: An Overview of RMI Applications https://docs.oracle.com/javase/tutorial/rmi/overview.html
 */
package chatclient;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface allows an implementing class to receive messages using RMI
 */
public interface ClientCallback extends Remote{

    /**
     * Receives message from server and prints it to the console
     * @param message: message to receive
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    void receiveMessage(String message) throws RemoteException;

}
