/**
 * CS262 Assignment 1
 * References: Remote Method Invocation and Object Serialization reading from class
 *             Oracle Tutoral: An Overview of RMI Applications https://docs.oracle.com/javase/tutorial/rmi/overview.html
 */

package chatclient;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import chatserver.ChatServer;

/**
 * A class to instantiate a client for the chat server. Any number of clients can exist simultanously. Its interactions
 * with the chat server are described at the package level.
 */
public class Client implements ClientCallback{

    /**
     * name of client
     */
    private String name;
    /**
     * Chatserver object the client is connected to in order to make RMI calls
     */
    private ChatServer server;
    /**
     * Stub that can be exported to allow server to make RMI calls to client to pass messages back
     */
    private ClientCallback myStub;

    /**
     * Checks if machine is windows or not
     * @return True if executed on a windows machine, false otherwise
     */
    static private Boolean isWindows()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }

    /**
     * Prints colored output for messages on non-windows machines (cmd can't handle coloration)
     * @param output string to output
     */
    static private void PrintlnResponse(String output) {
        if (!isWindows())
            System.out.print((char)27 + "[1m" + (char)27 + "[34m");
        System.out.println(output);
        if (!isWindows())
            System.out.print((char)27 + "[0m");
    }

    /**
     * Prints colored output for errors on non-windows machines (cmd can't handle coloration)
     * @param output string to output
     */
    static private void PrintlnError(String output) {
        if (!isWindows())
            System.out.print((char)27 + "[1m" + (char)27 + "[31m");
        System.out.println(output);
        if (!isWindows())
            System.out.print((char)27 + "[0m");
    }

    /**
     * Logs accountName into server, creating account accountName if it does not already exist
     * @param	 fromHost	 Hostname of registry host to connect to
     * @param	 accountName	 Name of account to login to
     */
    public void login(String fromHost, String accountName){
        try {
            server = getServer(fromHost);
            if (server != null) {
                if(!server.checkForAccount(accountName)){
                    server.addAccount(accountName);
                }
                server.login(accountName, myStub);
                name = accountName;
            }
            else {
                System.out.println("Server not found. Check your network connection and that you have specified the correct server ip address.");
                System.exit(0);
            }
        }
        catch (RemoteException e) {
            System.out.println("Unable to complete request due to communication failure. Check your network connection and the server.");
            System.exit(0);
        }
    }

    /**
     * Logs account corresponding to Client object out of server
     */
    public void logout(){
        try {
            server.logout(name);
        }
        catch(RemoteException e){
            System.out.println("Unable to communicate with server. Check your network connection and the server. You have not been logged out.");
        }
    }

    /**
     * Adds accountName as an account on the server
     * @param	 accountName	 account to add to the server
     */
    public void addAccount(String accountName){
        try {
            server.addAccount(accountName);
            System.out.println("Account added");
        }
        catch (RemoteException e){
            if (e.getCause() instanceof Error) {
                PrintlnError(e.getCause().getMessage());
            } else {
                System.out.println("Unable to communicate with server. Check your network connection and the server. The account was not added.");
            }
        }
    }

    /**
     * Adds a group to the server with name accountName
     * @param	 groupName	 name of group to add to the server
     */
    public void addGroup(String groupName){
        try {
            server.addGroup(groupName);
            System.out.println("Group added");
        }
        catch (RemoteException e){
            if (e.getCause() instanceof Error) {
                PrintlnError(e.getCause().getMessage());
            } else {
                System.out.println("Unable to communicate with server. Check your network connection and the server. The group was not added.");
            }
        }
    }

    /**
     * Adds an account username to group groupName
     * @param	 groupName	 name of group to add the member to
     * @param	 username	 name of user to add to the group
     */
    public void addGroupMember(String groupName, String username){
        try {
            server.addGroupMember(groupName, username);
            System.out.println("Group member added");
        }
        catch (RemoteException e){
            if (e.getCause() instanceof Error) {
                PrintlnError(e.getCause().getMessage());
            } else {
                System.out.println("Unable to communicate with server. Check your network connection and the server. The group member was not added.");
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * Lists all accounts on the server
     * @param	 query	 optional wildcard to return only a subset of accounts
     */
    public void listAccounts(String query) {
        List<String> accounts;
        try {
            accounts = server.listAccounts(query);
        } catch (RemoteException e) {
            System.out.println("Failed to retrieve account list. Unable to communicate with server. Check your network connection and the server.");
            return;
        }
        if (accounts.isEmpty()) {
            PrintlnError("No accounts found.");
        }
        for (String account : accounts) {
            PrintlnResponse(account);
        }
    }

    /**
     * Lists all groups on the server
     * @param	 query	 optional wildcard to return only a subset of groups
     */
    public void listGroups(String query) {
        List<String> accounts;
        try {
            accounts = server.listGroups(query);
        } catch (RemoteException e) {
            System.out.println("Failed to retrieve group list. Unable to communicate with server. Check your network connection and the server.");
            return;
        }
        if (accounts.isEmpty()) {
            PrintlnError("No accounts found.");
        }
        for (String account : accounts) {
            PrintlnResponse(account);
        }
    }

    /**
     * Sends a message to an account or group
     * @param	 target	 account or group name of intended message recipient
     * @param	 message	 message to send
     */
    public void sendMessage(String target, String message){
        try {
            if(server.checkForAccount(target)) {
                server.sendMessage(target, message);
            }
            else{
                System.out.println("Cannot send message. No such recipient.");
            }
        }
        catch (RemoteException e){
            System.out.println("Unable to communicate with server. Check your network connection and the server. Message not sent.");
        }
    }

    /**
     * Deletes an account
     * @param	 accountName	 name of account to delete
     */
    public void deleteAccount(String accountName){
        try {
            int ret = server.deleteAccount(accountName);
            if (ret == 0) {
                System.out.println("Account Deleted");
            }
            else if (ret == -1) {
                System.out.println("No such account");
            }
        }
        catch (RemoteException e){
            System.out.println("Unable to communicate with server. Check your network connection and the server. Account not deleted.");
        }
    }

    /**
     * Receives a message from the server and prints it to the console
     * This method is intended to be called over RMI by the chat server being used to send the message
     * @param	 message	 message to receive
     */
    @Override
    public void receiveMessage(String message){
        System.out.println(message);
    }

    /**
     * Gets reference to server for RMI calls and exports client stub to use for callbacks
     * Postconditition: security manager initialized
     * Postcondition: client stub is exported and reference is assigned to class variable myStub
     * @param	 fromHost	 Hostname of registry host to connect to
     * @return Object from the chatserver interface that can be used to call methods from that interface using RMI
     */
    private ChatServer getServer(String fromHost){
        if (System.getSecurityManager() ==  null){
            System.setSecurityManager(new SecurityManager());
        }
        try {
            myStub = (ClientCallback) UnicastRemoteObject.exportObject(this, 0);
            Registry useRegistry = LocateRegistry.getRegistry(fromHost);
            return((ChatServer) useRegistry.lookup("ChatServer"));
        }
        catch (Exception e){
            return null;
        }
    }

    /**
     * Core execution loop that takes commands from the user and executes the appropriate methods above
     * @param	 args	 Takes the server hostname as the first argument
     */
    public static void main(String [] args)
    {
        if (args.length == 0) {
            System.out.println("Please provide the Server hostname as the first argument");
            return;
        }
        System.out.println("Please log in. Enter an existing or new username:");
        String input = System.console().readLine();
        Client a = new Client();
        a.login(args[0], input);
        System.out.println("You can now enter commands");
        while(true){
            String[] command = System.console().readLine().split(" ", 3);
            if(command[0].equals("AddAccount")){
                if (command.length != 2)
                    PrintlnError("Syntax: AddAccount name");
                else {
                    a.addAccount(command[1]);
                }
            }
            else if(command[0].equals("ListAccounts"))
            {
                a.listAccounts(command.length > 1 ? command[1] : "");
            }
            else if(command[0].equals("ListGroups"))
            {
                a.listGroups(command.length > 1 ? command[1] : "");
            }
            else if(command[0].equals("AddGroup"))
            {
                if (command.length != 2)
                    PrintlnError("Syntax: AddGroup name");
                else
                    a.addGroup(command[1]);
            }
            else if(command[0].equals("AddGroupMember"))
            {
                if (command.length != 3)
                    PrintlnError("Syntax: AddGroupMember name user");
                else
                    a.addGroupMember(command[1],command[2]);
            }
            else if(command[0].equals("Send")){
                a.sendMessage(command[1], command[2]);
            }
            else if(command[0].equals("DeleteAccount")){
                if (command.length != 2)
                    PrintlnError("Syntax: DeleteAccount name");
                else
                    a.deleteAccount(command[1]);
            }
            else if(command[0].equals("Logout")){
                a.logout();
                System.exit(0);
            }
            else{
                System.out.println("Not a valid command");
            }
        }
    }
}
