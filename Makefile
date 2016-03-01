%.class: %.java
	javac $^
chatclient/Client.class: chatclient/Client.java chatserver/ChatServer.class chatserver/Server.class
	javac chatclient/Client.java
run-server: chatclient/Client.class chatclient/ClientCallback.class chatserver/ChatServer.class chatserver/Server.class
	java -cp . -Djava.rmi.server.hostname=localhost -Djava.security.policy=client.policy chatserver.Server