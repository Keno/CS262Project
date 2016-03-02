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
 * CS262 Assignment 1
 */
public class Server implements ChatServer{

    private HashMap<String, ClientCallback> accounts = new HashMap<String, ClientCallback>();
    private Registry registry;
    private ChatServer myStub;

    /**
     * Class containing a set of accounts. Overrides the receiveMessage function in the client to send messages to all clients in the group
     */
    public class Group implements ClientCallback {
        private Server server;
        private Set<String> members;

        public Group(Server TheServer) {
            server = TheServer;
            members = new HashSet<String>();
        }

        /**
         * Receives a message from the server and resends it to all members of the group
         * @param message: message to receive
         * @throws RemoteException
         */
        @Override
        public void receiveMessage(String message) throws RemoteException
        {
            List<String> deadMembers = new ArrayList<String>();
            for (String member : members) {
                server.sendMessage(member, message);
            }
        }

        /**
         * Adds a member to the group
         * @param member: member to add to the group
         * @throws Error
         */
        public void addMember(String member) throws Error
        {
            if (!server.checkForAccount(member))
                throw new Error("No such account");
            if (server.accounts.get(member) instanceof Group)
                throw new Error("Cannot add one group to another");
            members.add(member);
        }

        /**
         * Removes a member from a group if was a member before (i.e.
         * passing an account that's not a member is not an error)
         * @param member: member to remove from the group
         */
        public void removeIfMember(String member) throws Error
        {
            members.remove(member);
        }
    }

    /**
     * A ClientCallback that implements message queuing while the client is away.
     */
    public class Mailbox implements ClientCallback {
        private List<String> messages;

        public Mailbox() {
            messages = new ArrayList<String>();
        }

        @Override
        public void receiveMessage(String message) throws RemoteException
        {
            messages.add(message);
        }

        public void deliverMessages(ClientCallback to) throws RemoteException
        {
            for (String message : messages)
                to.receiveMessage(message);
        }
    }


    /**
     * Adds a group member to a group
     * @param groupName: name of group to add the member to
     * @param accountName: name of account to add to group
     * @throws RemoteException
     */
    @Override
    public void addGroupMember(String groupName, String accountName) throws RemoteException
    {
        if (!(accounts.get(groupName) instanceof Group))
            throw new Error("Not a group");
        ((Group)accounts.get(groupName)).addMember(accountName);
    }

    /**
     * Checks if an account exists on the server
     * @param accountName: account to check for
     * @return
     */
    @Override
    public Boolean checkForAccount(String accountName){
        return (accounts.containsKey(accountName));
    }

    //on login, key/value pair of client name/reference to client is added to accounts.
    //This is later used for lookup to send messages to that client
    /**
     * Logs an account with the given name into the server and associates it with a ClientCallback
     * @param id: name of account to log in
     * @param client: reference to object with ClientCallback interface
     */
    @Override
    public void login(String id, ClientCallback client){
        ClientCallback old = accounts.put(id, client);
        if (old instanceof Mailbox) {
            try {
                ((Mailbox)old).deliverMessages(client);
            } catch (RemoteException e) {
                // If delivery of queued messages failed, put back the mail box
                // until the client wants to login again.
                accounts.put(id, old);
            }
        }
    }

    /**
     * Logs out an account with the given name
     * @param id: name of account to log out
     */
    public void logout(String id){
        accounts.put(id, new Mailbox());
    }

    /**
     * Method to add account or group to the HashMap storing account and group names
     * @param accountName
     * @param x
     * @throws RemoteException
     */
    private void _addAccount(String accountName, ClientCallback x) throws RemoteException {
        if (accounts.containsKey(accountName)) {
            throw new Error("Account name already exists");
        }
        accounts.put(accountName, x);
    }

    /**
     * Adds an account to the server
     * @param accountName: name of account to add
     * @throws RemoteException
     */
    @Override
    public void addAccount(String accountName) throws RemoteException {
        _addAccount(accountName, new Mailbox());
    }

    /**
     * Adds a group to the server
     * @param groupName: name of group to add
     * @throws RemoteException
     */
    @Override
    public void addGroup(String groupName) throws RemoteException {
        _addAccount(groupName, new Group(this));
    }

    /**
     * Method to list accounts or groups, with an optional parameter query which lists a subset of accounts or groups by wildcard
     * @param query: query with wildcard to return a subset of all groups
     * @param groups
     * @return
     * @throws RemoteException
     */
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

    /**
     * Lists accounts on the server, with an optional parameter query which lists a subset of accounts by wildcard
     * @param query: query with wildcard to return a subset of all groups
     * @return
     * @throws RemoteException
     */
    @Override
    public List<String> listAccounts(String query) throws RemoteException {
        return _listAccounts(query, false);
    }

    /**
     * Lists groups on the server, with an optional parameter query which lists a subset of accounts by wildcard
     * @param query: query with wildcard to return a subset of all groups
     * @return
     * @throws RemoteException
     */
    @Override
    public List<String> listGroups(String query) throws RemoteException {
        return _listAccounts(query, true);
    }

    /**
     * Deletes an account
     * @param accountName: name of account to delete
     * @return: 0 if successful and -1 if account does not exist
     */
    public int deleteAccount(String accountName){
        if(accounts.containsKey(accountName)){
            // First remove the account from all groups
            for (ClientCallback receiver : accounts.values())
                if (receiver instanceof Group)
                    ((Group)receiver).removeIfMember(accountName);
            accounts.remove(accountName);
            return 0;
        }
        else{
            return -1;
        }
    }

    /**
     * Sends a message to a given client or group of clients
     * @param accountName: name of account or group to send the message to
     * @param message: message to send
     */
    @Override
    public void sendMessage(String accountName, String message) {
        ClientCallback targetClient = accounts.get(accountName);
        Boolean IsClient = !(targetClient instanceof Group) &&
            !(targetClient instanceof Mailbox);
        try {
            targetClient.receiveMessage(message);
        }
        catch (RemoteException e){
            System.out.println("Server unable to reach a logged in client.");
            // If this was an actual client, log out that client and deliver
            // the message to the mailbox.
            if (IsClient) {
                logout(accountName);
                sendMessage(accountName, message);
            }
        }
    }

    /**
     * Adds server to RMI registry and exports stub so that clients can access the server
     * @throws RemoteException
     */
    public void exportServer() throws RemoteException {
        if (System.getSecurityManager() ==  null){
            System.setSecurityManager(new SecurityManager());
        }
        registry = LocateRegistry.getRegistry();
        myStub = (ChatServer) UnicastRemoteObject.exportObject(this,0);
        registry.rebind("ChatServer", myStub);
    }

    /**
     * Binds server upon startup
     * @param args
     */
    public static void main(String[] args) {
        Server a = new Server();
        try {
            a.exportServer();
            System.out.println("Server bound. Ready for use.");
        }
        catch (Exception e)
        {
            System.out.println("Server was not bound properly.");
            e.printStackTrace(System.out);
        }
    }
}
