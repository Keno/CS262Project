compute/%.class: compute/%.java
	javac $^
%/%.class: %/%.java
	javac -cp compute.jar $^
compute.jar: compute/Compute.class compute/Task.class
	jar cvf compute.jar $^
run-server: engine/ComputeEngine.java
	java -cp .:$(pwd)/compute.jar -Djava.rmi.server.codebase=file:$(pwd)/compute.jar -Djava.rmi.server.hostname=localhost -Djava.security.policy=server.policy engine.ComputeEngine