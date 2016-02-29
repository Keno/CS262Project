package chatserver;

import chatclient.Client;
import chatclient.ClientCallback;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;

/**
 * Created by Aaron on 2/27/2016.
 */
public class Server implements ChatServer{

    private HashMap<String, ClientCallback> accounts = new HashMap<String, ClientCallback>();
    private Registry registry;
    private ChatServer myStub;

    public class Group implements ClientCallback {
        private Server server;
        private Set<String> members;

        public Group(Server TheServer) {
            server = TheServer;
            members = new HashSet<String>();
        }

        @Override
        public void receiveMessage(String message) throws RemoteException
        {
            for (String member : members) {
                server.sendMessage(member, message);
            }
        }

        public void addMember(String member) throws Error
        {
            if (!server.checkForAccount(member))
                throw new Error("No such account");
            if (server.accounts.get(member) instanceof Group)
                throw new Error("Cannot add one group to another");
            members.add(member);
        }
    }

    @Override
    public void addGroupMember(String groupName, String accountName) throws RemoteException
    {
        if (!(accounts.get(groupName) instanceof Group))
            throw new Error("Not a group");
        ((Group)accounts.get(groupName)).addMember(accountName);
    }

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

    private void _addAccount(String accountName, ClientCallback x) throws RemoteException {
        if (accounts.containsKey(accountName)) {
            throw new Error("Account name already exists");
        }
        accounts.put(accountName, x);
    }

    @Override
    public void addAccount(String accountName) throws RemoteException {
        _addAccount(accountName, null);
    }

    @Override
    public void addGroup(String groupName) throws RemoteException {
        _addAccount(groupName, new Group(this));
    }

    private List<String> _listAccounts(String query, Boolean groups) throws RemoteException {
        // MVP
        List<String> keys = (new ArrayList<String>(accounts.keySet()));
        keys = keys.stream()
            .filter(k ->
                !(groups ^ (accounts.get(k) instanceof Group)) &&
                 (query.isEmpty() || k.matches(query)))
            .collect(Collectors.toList());
        return keys;
    }

    @Override
    public List<String> listAccounts(String query) throws RemoteException {
        return _listAccounts(query, false);
    }

    @Override
    public List<String> listGroups(String query) throws RemoteException {
        return _listAccounts(query, true);
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
