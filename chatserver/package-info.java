/**
 * This package provides an interface and class for the server for a chat application.
 *
 * The {@link chatserver.Server} class implements a server, which contains a list of active chat accounts, each of which
 * can represent an individual or a group. The Server
 * implements the {@link chatserver.ChatServer} interface, which contains RMI-callable methods that allow a client to
 * manipulate this list, such as by adding accounts through {@link chatserver.Server#addAccount(java.lang.String)}, as
 * well as sending a message to another account using {@link chatserver.Server#sendMessage(java.lang.String, java.lang.String)}.
 * The list is saved as a Hashmap {@link chatserver.Server#accounts} that pairs account names with ClientCallback objects.
 * When a client logs in to the server using {@link chatserver.Server#login(java.lang.String, chatclient.ClientCallback)},
 * it passes the server a stub pointing to itself so the server can call rmi methods from that client. When the server
 * wants to send a message, it only receives an account name from the sending client. By matching the account name to the
 * ClientCallback object, the server can send the desired message using an rmi call to {@link chatclient.Client#receiveMessage(java.lang.String)}
 * in the client code, acting here as a client to deliver the message.The use of the HashMap allows constant lookup time
 * of random items regardless of the number of accounts the server has, making it a good way to store this information
 * for this use.
 */
package chatserver;