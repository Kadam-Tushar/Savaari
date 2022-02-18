# Savaari-Akka-Phase-2


Commands to test :

1. Start all the nodes  - On each new terminal 
```
mvn exec:java -Dexec.mainClass="pods.cabs.Main" -Dexec.args=10001
mvn exec:java -Dexec.mainClass="pods.cabs.Main" -Dexec.args=10002
mvn exec:java -Dexec.mainClass="pods.cabs.Main" -Dexec.args=10003
mvn exec:java -Dexec.mainClass="pods.cabs.Main" -Dexec.args=10004
mvn test

```