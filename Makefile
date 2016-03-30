%.class: %.java
	javac $^
chatclient/Client.class: chatclient/Client.java chatserver/ChatServer.class chatserver/Server.class
	javac chatclient/Client.java
CLASSES := chatclient/Client.class chatclient/ClientCallback.class chatserver/ChatServer.class chatserver/Server.class
EXT_IP = $(shell ifconfig en0 | grep inet | grep -v inet6 | awk '{print $$2}')
docs:
	javadoc -doclet ch.raffael.doclets.pegdown.PegdownDoclet -docletpath pegdown-doclet-1.2.1-all.jar -splitindex -private -overview README.md chatclient chatserver -d $(shell pwd)/Documentation/
run-server-local: $(CLASSES)
	java -cp . -Djava.rmi.server.hostname=localhost -Djava.rmi.server.codebase=file:///$(pwd) -Djava.security.policy=keno.policy chatserver.Server
run-server-remote: $(CLASSES)
	java -cp . -Djava.rmi.server.hostname=$(EXT_IP) -Djava.rmi.server.codebase=file:///$(pwd) -Djava.security.policy=keno.policy chatserver.Server
run-client-local: $(CLASSES)
    java -cp . -Djava.rmi.server.hostname=localhost -Djava.rmi.server.codebase=file:///$(pwd) -Djava.security.policy=keno.policy chatclient.Client localhost
default: $(CLASSES)
