/**
 * CS262 Assignment 1
 * References: Remote Method Invocation and Object Serialization reading from class
 *             Oracle Tutoral: An Overview of RMI Applications https://docs.oracle.com/javase/tutorial/rmi/overview.html
 */
package chatserver;

import chatclient.ClientCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This interface, with all methods to be called by the clients over RMI, includes the methods needed in the server for
 * the clients to be able to manipulate the list of accounts and to send messages
 */
public interface ChatServer extends Remote {

    /**
     * Checks if an account exists on the server.
     * @param	 accountName	 account to check for
     * @return True if account exists or false if it does not
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    Boolean checkForAccount(String accountName) throws RemoteException;

    /**
     * Logs an account with the given name into the server and associates it with a ClientCallback
     * @param	 id	 name of account to log in
     * @param	 client	 reference to object with ClientCallback interface
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    void login(String id, ClientCallback client) throws RemoteException;

    /**
     * Logs out an account with the given name
     * @param	 id	 name of account to log out
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    void logout(String id) throws RemoteException;

    /**
     * Adds an account to the server
     * @param	 accountName	 name of account to add
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    void addAccount(String accountName) throws RemoteException;

    /**
     * Lists accounts on the server, with an optional parameter query which lists a subset of accounts by wildcard
     * @param	 query	 query with wildcard to return a subset of all groups
     * @return List of strings containing the requested accounts
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    List<String> listAccounts(String query) throws RemoteException;

    /**
     * Adds an empty group to the server
     * @param	 groupName	 name of group to add
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    void addGroup(String groupName) throws RemoteException;

    /**
     * Adds a group member to a group
     * @param	 groupName	 name of group to add the member to
     * @param	 accountName	 name of account to add to group
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    void addGroupMember(String groupName, String accountName) throws RemoteException;

    /**
     * Lists groups on the server, with an optional parameter query which lists a subset of accounts by wildcard
     * @param	 query	 query with wildcard to return a subset of all groups
     * @return List of strings containing the requested accounts
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    List<String> listGroups(String query) throws RemoteException;

    /**
     * Sends a message to a given client or group of clients
     * @param	 accountName	 name of account or group to send the message to
     * @param	 message	 message to send
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    void sendMessage(String accountName, String message) throws RemoteException;

    /**
     * Deletes an account from the server
     * @param	 accountName	 name of account to delete
     * @return 0 if successful and -1 if account does not exist
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    int deleteAccount(String accountName) throws RemoteException;

}
