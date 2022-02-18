FROM openjdk:11-jdk-slim
COPY h2-1.4.200.jar app.jar
CMD  java -cp app.jar org.h2.tools.Server -webAllowOthers -tcpAllowOthers -ifNotExists
