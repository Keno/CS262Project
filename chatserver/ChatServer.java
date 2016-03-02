package chatserver;

import chatclient.ClientCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * CS262 Assignment 1
 * References: Remote Method Invocation and Object Serialization reading from class
 *             Oracle Tutoral: An Overview of RMI Applications https://docs.oracle.com/javase/tutorial/rmi/overview.html
 */
public interface ChatServer extends Remote{

    /**
     * Checks if an account exists on the server
     * @param accountName: account to check for
     * @return: True if account exists or false if it does not
     * @throws RemoteException
     */
    Boolean checkForAccount(String accountName) throws RemoteException;

    /**
     * Logs an account with the given name into the server and associates it with a ClientCallback
     * @param id: name of account to log in
     * @param client: reference to object with ClientCallback interface
     * @throws RemoteException
     */
    void login(String id, ClientCallback client) throws RemoteException;

    /**
     * Logs out an account with the given name
     * @param id: name of account to log out
     * @throws RemoteException
     */
    void logout(String id) throws RemoteException;

    /**
     * Adds an account to the server
     * @param accountName: name of account to add
     * @throws RemoteException
     */
    void addAccount(String accountName) throws RemoteException;

    /**
     * Lists accounts on the server, with an optional parameter query which lists a subset of accounts by wildcard
     * @param query: query with wildcard to return a subset of all groups
     * @return: List of strings containing the requested accounts
     * @throws RemoteException
     */
    List<String> listAccounts(String query) throws RemoteException;

    /**
     * Adds an empty group to the server
     * @param groupName: name of group to add
     * @throws RemoteException
     */
    void addGroup(String groupName) throws RemoteException;

    /**
     * Adds a group member to a group
     * @param groupName: name of group to add the member to
     * @param accountName: name of account to add to group
     * @throws RemoteException
     */
    void addGroupMember(String groupName, String accountName) throws RemoteException;

    /**
     * Lists groups on the server, with an optional parameter query which lists a subset of accounts by wildcard
     * @param query: query with wildcard to return a subset of all groups
     * @return: List of strings containing the requested accounts
     * @throws RemoteException
     */
    List<String> listGroups(String query) throws RemoteException;

    /**
     * Sends a message to a given client or group of clients
     * @param accountName: name of account or group to send the message to
     * @param message: message to send
     * @throws RemoteException
     */
    void sendMessage(String accountName, String message) throws RemoteException;

    /**
     * Deletes an account from the server
     * @param accountName: name of account to delete
     * @return: 0 if successful and -1 if account does not exist
     * @throws RemoteException
     */
    int deleteAccount(String accountName) throws RemoteException;

}
