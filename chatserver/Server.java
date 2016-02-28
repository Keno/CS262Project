package chatserver;

import chatclient.Client;
import chatclient.ClientCallback;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;


/**
 * Created by Aaron on 2/27/2016.
 */
public class Server implements ChatServer{

    private HashMap<String, ClientCallback> accounts = new HashMap<String, ClientCallback>();
    private Registry registry;
    private ChatServer myStub;

    @Override
    public Boolean checkForAccount(String accountName){
        return (accounts.containsKey(accountName));
    }

    //on login, key/value pair of client name/pointer to client is added to accounts.
    //This is later used for lookup to send messages to that client
    @Override
    public void login(String id, ClientCallback client){
        accounts.put(id, client);
    }

    public void logout(String id){
        accounts.put(id, null);
    }

    @Override
    public int addAccount(String accountName){
        if(accounts.containsKey(accountName)){
            return -1;
        }
        else{
            accounts.put(accountName, null);
            return 0;
        }
    }

    public int deleteAccount(String accountName){
        if(accounts.containsKey(accountName)){
            accounts.remove(accountName);
            return 0;
        }
        else{
            return -1;
        }
    }

    public void sendMessage(String accountName, String message){
        ClientCallback targetClient = accounts.get(accountName);
        try {
            targetClient.receiveMessage(message);
        }
        catch (RemoteException e){
            System.out.println("Message Send Failed: Server");
        }
    }

    public void exportServer() throws RemoteException {
        if (System.getSecurityManager() ==  null){
            System.setSecurityManager(new SecurityManager());
        }
        registry = LocateRegistry.getRegistry();
        myStub = (ChatServer) UnicastRemoteObject.exportObject(this,0);
        registry.rebind("ChatServer", myStub);
    }

    public static void main(String[] args) {
        Server a = new Server();
        try {
            a.exportServer();
            System.out.println("Server bound");
        }
        catch (Exception e)
        {
            System.out.println("Binding Failed");
        }
    }
}
