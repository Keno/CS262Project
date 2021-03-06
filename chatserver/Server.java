/**
 * CS262 Assignment 1
 * References: Remote Method Invocation and Object Serialization reading from class
 *             Oracle Tutoral: An Overview of RMI Applications https://docs.oracle.com/javase/tutorial/rmi/overview.html
 *
 *
 */

package chatserver;

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
 * A class to instantiate a chat server for any number of clients. It's interactions with the clientsr are described at
 * the package level.
 * All override methods, all of which throw remoteexceptions, are intended to be called over RMI from the client code
 */
public class Server implements ChatServer{

    /**
     * Hashmap that pairs account names to the corresponding ClientCallback object
     * needed to send messages to that account (user, group, etc).
     *
     * The use of the HashMap allows constant lookup time
     * of random items regardless of the number of accounts the server has,
     * making it a good way to store this information for this use.
     */
    private HashMap<String, ClientCallback> accounts = new HashMap<String, ClientCallback>();

    /**
     * RMI registry the server is registered to
     */
    private Registry registry;
    /**
     * Stub that can be exported to allow client to make RMI calls to the server
     */
    private ChatServer myStub;

    /**
     * Class containing a set of accounts. Overrides the receiveMessage function in the client to send messages
     * to all clients in the group.
     */
    public class Group implements ClientCallback {
        /**
         * The Server on which on which this group lives. This is used in receiveMessage to
         * broadcast received messages to the members of the group.
         */
        private Server server;
        
        /**
         * The account names of all accounts that any messages received by this
         * group should be re-broadcast to. At all times, the accounts referenced
         * here MUST refer to user accounts not group accounts.
         */
        private Set<String> members;

        public Group(Server TheServer) {
            server = TheServer;
            members = new HashSet<String>();
        }

        /**
         * Receives a message from the server and broadcasts it to all members of the group.
         *
         * It is important to note that this method calls back to the server object to perform
         * the actual delivery of the messages, in order to properly handle all cases, e.g.
         * a dropped client connection.
         *
         * @param	 message	 message to receive
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
         * @param	 member	 member to add to the group
         * @throws Error with appropriate message if adding the member fails
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
         * passing an account that's not a member is not an error).
         *
         * Note that this message MUST be called whenever an account that could
         * have been a member of this group is removed in order to enforce that
         * a group may not contain other groups as members (if this method is
         * not called, a user account could be deleted and recreated as a group,
         * leading to dispatch loops).
         *
         * @param	 member	 member to remove from the group
         */
        public void removeIfMember(String member) throws Error
        {
            members.remove(member);
        }
    }

    /**
     * A ClientCallback that implements message queuing while the client is away.
     * The server substitutes in an instance of this class as a placeholder
     * whenever a client disconnects from the server (either voluntarily or
     * forcibly due to an error condition). Once the client reconnects, the
     * server invokes the deliverMessages method, instructing the Mailbox to
     * deliver its messages. If this succeeds, the Mailbox will be removed, and
     * the client will once again be reachable by its account name. If message
     * delivery fails, the mailbox remains associated with the account name and
     * keeps accumulating messages.
     */
    public class Mailbox implements ClientCallback {
        /**
         * The messages queued in this mailbox
         */
        private List<String> messages;

        public Mailbox() {
            messages = new ArrayList<String>();
        }

        /**
         * Queue a message for later delivery
         */
        @Override
        public void receiveMessage(String message) throws RemoteException
        {
            messages.add(message);
        }

        /**
         * Deliver all queued messages to the specified client
         *
         * @param to the client to deliver the messages to
         */
        public void deliverMessages(ClientCallback to) throws RemoteException
        {
            for (String message : messages)
                to.receiveMessage(message);
        }
    }


    /**
     * Adds a group member to a group
     * @param	 groupName	 name of group to add the member to
     * @param	 accountName	 name of account to add to group
     * @throws RemoteException on RMI failure. Check connection to server.
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
     * @param	 accountName	 account to check for
     * @return True if account exists, false if it does not
     */
    @Override
    public Boolean checkForAccount(String accountName){
        return (accounts.containsKey(accountName));
    }

    /**
     * Logs an account with the given name into the server and associates it
     * with a ClientCallback.
     *
     * Furthermore, we will attempt to deliver any queued messages if there was
     * a mailbox associated with this account name.
     *
     * @param	 id	 name of account to log in
     * @param	 client	 reference to object with ClientCallback interface
     */
    @Override
    public void login(String id, ClientCallback client){
       //on login, key/value pair of client name/reference to client is added to accounts.
       //This is later used for lookup to send messages to that client
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
     *
     * The account name will now refer to a mailbox instead, queueing up messages
     * until the next time the user reconnects to this server.
     *
     * @param	 id	 name of account to log out
     */
    public void logout(String id){
        accounts.put(id, new Mailbox());
    }

    /**
     * Method to add account or group to the HashMap storing account and group names
     * @param	 accountName	 name of account to add
     * @param	 x	 ClientCallback object corresponding to account name, which provides a reference so server can send messages
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    private void _addAccount(String accountName, ClientCallback x) throws RemoteException {
        if (accounts.containsKey(accountName)) {
            throw new Error("Account name already exists");
        }
        accounts.put(accountName, x);
    }

    /**
     * Adds an account to the server
     * @param	 accountName	 name of account to add
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    @Override
    public void addAccount(String accountName) throws RemoteException {
        _addAccount(accountName, new Mailbox());
    }

    /**
     * Adds a group to the server
     * @param	 groupName	 name of group to add
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    @Override
    public void addGroup(String groupName) throws RemoteException {
        _addAccount(groupName, new Group(this));
    }

    /**
     * Method to list accounts or groups, with an optional parameter query which lists a subset of accounts or groups by wildcard
     * @param	 query	 query with wildcard to return a subset of all groups
     * @param	 groups	
     * @return list of accounts or groups
     * @throws RemoteException on RMI failure. Check connection to server.
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
     * @param	 query	 query with wildcard to return a subset of all groups
     * @return list of accounts on server
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    @Override
    public List<String> listAccounts(String query) throws RemoteException {
        return _listAccounts(query, false);
    }

    /**
     * Lists groups on the server, with an optional parameter query which lists a subset of accounts by wildcard
     * @param	 query	 query with wildcard to return a subset of all groups
     * @return list of groups
     * @throws RemoteException on RMI failure. Check connection to server.
     */
    @Override
    public List<String> listGroups(String query) throws RemoteException {
        return _listAccounts(query, true);
    }

    /**
     * Deletes an account
     * @param	 accountName	 name of account to delete
     * @return 0 if successful and -1 if account does not exist
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
     * @param	 accountName	 name of account or group to send the message to
     * @param	 message	 message to send
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
     * @throws RemoteException on RMI failure. Check connection to server.4
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
