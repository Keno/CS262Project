package chatclient;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.List;

import chatserver.ChatServer;

/**
 * Created by Aaron on 2/27/2016.
 */
public class Client implements ClientCallback{

    private String name;
    private ChatServer server;
    private ClientCallback myStub;

    static private void PrintlnResponse(String output) {
        System.out.print((char)27 + "[1m" + (char)27 + "[34m");
        System.out.println(output);
        System.out.print((char)27 + "[0m");
    }

    static private void PrintlnError(String output) {
        System.out.print((char)27 + "[1m" + (char)27 + "[31m");
        System.out.println(output);
        System.out.print((char)27 + "[0m");
    }

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
                System.out.println("Can't find server");
            }
        }
        catch (RemoteException e) {
            System.out.println("Failed");
            e.printStackTrace(System.out);
        }
    }

    public void logout(){
        try {
            server.logout(name);
        }
        catch(RemoteException e){
            e.printStackTrace(System.out);
        }
    }

    public void addAccount(String accountName){
        try {
            server.addAccount(accountName);
        }
        catch (RemoteException e){
            if (e.getCause() instanceof Error) {
                PrintlnError(e.getCause().getMessage());
            } else {
                System.out.println("Can't connect to server");
                e.printStackTrace(System.out);
            }
        }
    }

    public void addGroup(String accountName){
        try {
            server.addGroup(accountName);
        }
        catch (RemoteException e){
            if (e.getCause() instanceof Error) {
                PrintlnError(e.getCause().getMessage());
            } else {
                System.out.println("Can't connect to server");
                e.printStackTrace(System.out);
            }
        }
    }

    public void addGroupMember(String groupName, String username){
        try {
            server.addGroupMember(groupName, username);
        }
        catch (RemoteException e){
            if (e.getCause() instanceof Error) {
                PrintlnError(e.getCause().getMessage());
            } else {
                System.out.println("Can't connect to server");
                e.printStackTrace(System.out);
            }
        }
    }

    public void listAccounts(String query) {
        List<String> accounts;
        try {
            accounts = server.listAccounts(query);
        } catch (RemoteException e) {
            System.out.println("Failed to retrieve account list");
            e.printStackTrace(System.out);
            return;
        }
        if (accounts.isEmpty()) {
            PrintlnError("No accounts found.");
        }
        for (String account : accounts) {
            PrintlnResponse(account);
        }
    }

    public void listGroups(String query) {
        List<String> accounts;
        try {
            accounts = server.listGroups(query);
        } catch (RemoteException e) {
            System.out.println("Failed to retrieve account list");
            e.printStackTrace(System.out);
            return;
        }
        if (accounts.isEmpty()) {
            PrintlnError("No accounts found.");
        }
        for (String account : accounts) {
            PrintlnResponse(account);
        }
    }

    public void sendMessage(String target, String message){
        try {
            server.sendMessage(target, message);
        }
        catch (RemoteException e){
            System.out.println("Message Send Failed: Client");
        }
    }

    public int deleteAccount(String accountName){
        try {
            return server.deleteAccount(accountName);
        }
        catch (RemoteException e){
            System.out.println("Can't connect to server");
            e.printStackTrace(System.out);
            return 1;
        }
    }

    @Override
    public void receiveMessage(String message){
        System.out.println(message);
    }

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
            System.out.println("Unable to find Server");
            return null;
        }
    }

    public static void main(String [] args)
    {
        if (args.length == 0) {
            System.out.println("Please provide the Server hostname as the first argument");
            return;
        }
        System.out.println("Please log in. Enter an existing or new username");
        String input = System.console().readLine();
        Client a = new Client();
        a.login(args[0], input);
        System.out.println("You can now enter commands");
        while(true){
            String[] command = System.console().readLine().split(" ", 3);
            if(command[0].equals("AddAccount")){
                a.addAccount(command[1]);
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
                int ret = a.deleteAccount(command[1]);
                if(ret == 0) {
                    System.out.println("Account Deleted");
                }
                else if(ret == -1){
                    System.out.println("No such account");
                }
            }
            else if(command[0].equals("Logout")){
                a.logout();
                break;
            }
            else{
                System.out.println("Not a valid command");
            }
        }
    }
}
