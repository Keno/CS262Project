%.class: %.java
	javac $^
chatclient/Client.class: chatclient/Client.java chatserver/ChatServer.class chatserver/Server.class
	javac chatclient/Client.java
CLASSES := chatclient/Client.class chatclient/ClientCallback.class chatserver/ChatServer.class chatserver/Server.class
EXT_IP = $(shell ifconfig en0 | grep inet | grep -v inet6 | awk '{print $$2}')
run-server-local: $(CLASSES)
	java -cp . -Djava.rmi.server.hostname=localhost -Djava.rmi.server.codebase=file:///Users/kfischer/Projects/CS262/ -Djava.security.policy=keno.policy chatserver.Server
run-server-remote: $(CLASSES)
	java -cp . -Djava.rmi.server.hostname=$(EXT_IP) -Djava.rmi.server.codebase=file:///Users/kfischer/Projects/CS262/ -Djava.security.policy=keno.policy chatserver.Server
