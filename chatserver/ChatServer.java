package chatserver;

import chatclient.Client;
import chatclient.ClientCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by Aaron on 2/27/2016.
 */
public interface ChatServer extends Remote{

    Boolean checkForAccount(String accountName) throws RemoteException;

    void login(String id, ClientCallback client) throws RemoteException;

    void logout(String id) throws RemoteException;

    //void logout(Client client) throws RemoteException;

    int addAccount(String accountName) throws RemoteException;

    /*List<String> listAccounts(String searchTerm) throws RemoteException;

    int addGroup(String groupName, Set<String> groupMembers) throws RemoteException;

    List<String> listGroups(String searchTerm) throws RemoteException;*/

    void sendMessage(String accountName, String message) throws RemoteException;

    int deleteAccount(String accountName) throws RemoteException;

}