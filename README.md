# CS262 Simple Chat Application

This project implements a simple client/server chat application using Java RMI.
The application includes two packages, {@link chatclient} for the client
and {@link chatserver} for the server.

The client is a simple terminal command parser and passes any command on to
the server using an RMI RPC.

The server implements the core message routing and any auxilliary state required to fulfill
that function, including keeping track of users, groups and queued but undelivered messages.

Finally, since the server needs to be able to asynchronously deliver messages to the client,
a separate 'callback' connection is kept between the chatclient and the chatserver, in which
the `chatclient` acts as the server, accepting messages to be delivered to the user.

# Getting Started
## Compiling the Application
For those users, with a sensible development environment, a Makefile is provided that will
automatically compile all required classes. However, installing manually is not very difficult
either. The following instructions should work on both Windows and Unix-compatible systems,
assuming the JDK is properly installed and available on the PATH.

```
    cd CS262Project
    javac chatclient/ClientCallback.java chatserver/ChatServer.java
    jar cvf chat.jar chatserver/ChatServer.class chatclient/ClientCallback.class
    javac -cp chat.jar chatserver/Server.java
    javac -cp chat.jar chatclient/Client.java
```
## Running the Application
### Starting the `rmiregistry`
First you will need to ensure that the RMI registry is running on the server machine.
By default the RMI registry will be bound to port 1999. On Windows, you may start the
the RMI registry using
```
start rmiregistry
```
while on Unix-compatible systems
```
rmiregistry &
```
should be sufficient. If port 1999 is unavailable or you wish to specify a different port,
you can pass the desired port as an argument to `rmiregistry`.

### Running the ChatServer

Before being able to run the ChatServer, you will have to adjust the policy file to match your
system. Two template from the authors' systems are provided in the toplevel directory. Pick
either file and adjust the path to match your checkout's configuration. From now on, we will
call this new policy file your.policy.

Now that the policy is created, you can launch the chatserver using

```
    java -cp chat.jar -Djava.rmi.server.codebase=file:chat.jar -Djava.rmi.server.hostname=<hostname> -Djava.security.policy=your.policy server.Server
```

where `<hostname>` should be replaced by a hostname that belongs to the server's host and is reachable from the client. If you are only interested in local connections, you may use `localhost` for `<hostname>`.

### Launching the ChatClinet

Then, on the client machine, we can connect to this server by running

```
java -cp chat.jar -Djava.rmi.server.hostname=<client-hostname> -Djava.security.policy=Keno.policy client.ChatClient <server-hostname>
```

where `<server-hostname>` needs to be the same hostname as given to the server in the previous step and `<client-hostname>` must be one of the client host's hostnames that is reachable by the server.


The client should then be connected to the server and can begin chatting.

# User Manual

On connecting, the user supplies an account name for the client. If it does not exist, it is created. If it does exist and there is an outstanding message queue, the messages are now to the client. After queued messages are delivered, the user may begin entering commands.

The valid commands are summarized in this table:

| Command                     | Effect                                                                                                                                                                                                                                                                                     |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AddAccount name             | Creates a new account named `name` on the server. It is an error to not specify a `name` or to specify a `name` that is already taken by an existing user or group.                                                                                                                        |
| ListAccounts [query]        | Lists the account names of all user accounts that are currently known to the server. Optionally, the user may specify a regular expression against which to match the account name. Only matching accounts names will be returned.                                                         |
| ListGroups [query]          | The functionality is identical to that of ListAccounts, with the exception that groups rather than user accounts are listed.                                                                                                                                                               |
| AddGroup name               | Creates a new group with the name `name`. It is an error to not specify a `name` or to specify a `name` that is already taken by an existing user or group.                                                                                                                                |
| AddGroupMember group member | Adds the user account `member` to the group `group`. It is an error for `group` not to name a valid group or `member` not to name a valid user account.                                                                                                                                    |
| Send account [message]      | Sends the message `message` to the user or group `denoted` by `account`. Any (direct or indirect) recipient that is currently connected to the server will receive the message immediately. Any non-connected recipient will receive the message as soon as they re-connect to the server. |
| DeleteAccount [name]        | Deletes the account designated by `[name]`.                                                                                                                                                                                                                                                |
| Logout                      | Ends the current session                                                                                                                                                                                                                                                                   |
| ^D                          | Same effect as Logout                                                                                                                                                                                                                                                                      |

Square brackets denote optional arguments.
