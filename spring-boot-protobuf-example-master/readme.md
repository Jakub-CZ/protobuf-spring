# Using Google Protocol Buffers for High Speed REST (V2)

Revised and updated Josh Longs original [Spring Boot Google Protocol Buffers](https://github.com/joshlong/spring-and-google-protocol-buffers) example project 
with Spring Boot 1.4.3.RELEASE an integrated protobuf-compiler maven setup.

The compilation of the protocol buffers specifications will be performed
during the maven build.


## Implementation notes

- ``ProtobufHttpMessageConverter`` encodes/decodes Protocol Buffer objects automatically
  - if the client specifies Accept: application/x-protobuf, for example, then our REST service will send back the Protocol Buffer-encoded response

## Build
To build the example just execute:
```
mvn clean package
```

## Run
To run the example just execute:
```
java -jar target/spring-protobuf-example-*-SNAPSHOT.jar
```