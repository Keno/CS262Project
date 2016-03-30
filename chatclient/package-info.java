/**
 * This package provides an interface and class for the client for a chat application.
 *
 * The {@link chatclient.Client} class implements a client for the {@link chatserver.Server}. The client contains
 * only its own name, a {@link chatserver.ChatServer} object pointing to the server that it wants to interact with, and
 * a {@link chatclient.ClientCallback} object that acts as a stub pointing to itself. On logging in to the server, it
 * give the server it's name and corresponding stub so the server can identify different clients by account name. As for
 * the client itself, nearly all of its methods, such as {@link chatclient.Client#login(java.lang.String, java.lang.String)}
 * or {@link chatclient.Client#sendMessage(java.lang.String, java.lang.String)}, are just wrappers for RMI calls to
 * server methods. The exception is {@link chatclient.Client#receiveMessage(java.lang.String)}, which the server calls
 * for the correct client using the exported stub when the client is to receive a message. This then displays the
 * message in the clients terminal.
 */
package chatclient;