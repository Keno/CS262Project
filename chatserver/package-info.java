/**
 * # The chatserver package
 *
 * This package implements the core business logic of the application.
 *
 * On the one hand, it contains the {@link chatserver.ChatServer}, which defines
 * the entrypoints available to clients. The functions available in this interface
 * essentially stand in 1-to-1 correspondance with the commands the users may
 * enter at the command prompt, e.g. {@link chatserver.Server#addAccount(java.lang.String)}
 * for adding a new account to the server. These commands are usually invoked by
 * the client immediately after command parsing and syntax validation.
 *
 * The {@link chatserver.Server} implements this protocol and contains the core
 * business logic. The {@link chatserver.Server} class maintains a list 
 * ({@link chatserver.Server#accounts}) of  active chat accounts, each of which
 * can represent an individual or a group.
 * Importantly, when routing messages, the server is agnostic to whether a
 * particular account is a user or a group. Both the client stub and the
 * {@link chatserver.Server.Group} implement the {@link chatclient.ClientCallback}
 * interface. If the account is a user, the message is sent via RMI to the client
 * in order to display it to the user. If the account is a group, the same code
 * will instead locally inoke the group's {@link chatserver.Server.Group#receiveMessage}
 * which will then pass the message on to its members in the appropriate fashion.
 *
 * The same mechanism is also used for offline users. When a user disconnects
 * from the server, or if message delivery fails for some other reason, their
 * entry in the account list is replaced by an instance of
 * {@link chatserver.Server.Mailbox}, whose receiveMessage method will simply
 * queue any messages sent to it until the user once again reconnects to the
 * server.
 */
package chatserver;
