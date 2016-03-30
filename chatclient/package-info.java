/**
 * # The chatclient package
 * 
 * This package provides an interface and class for the client for a chat application.
 *
 * The {@link chatclient.Client} class implements a client for the {@link chatserver.Server}.
 * The {@link chatclient.Client} primarily acts as a terminal command parser and an
 * means by which messages may be displayed to the user. As such, nearly all of its methods,
 * such as {@link chatclient.Client#login(java.lang.String, java.lang.String)} or
 * {@link chatclient.Client#sendMessage(java.lang.String, java.lang.String)}, 
 * are just wrappers for RMI calls to server methods. 
 * 
 * The one exception is {@link chatclient.Client#receiveMessage(java.lang.String)},
 * which is the implementation of the ClientCallback interface also specified in
 * this package. This method allos the server to send a message to the cleient to
 * be displayed in the users' terminal.
 */
package chatclient;
