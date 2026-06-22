# springboot-java8 (migrated to Quarkus)

Originally a Spring Boot reference app demonstrating Java 8 features, this project has been
migrated to [Quarkus](https://quarkus.io/) 3.x running on Java 17+. It still showcases the
same Java features and exposes the same REST endpoints for topic management and functional
programming examples.

It contains a hardcoded list of topics. You can call the APIs (e.g. with POSTMAN or curl) to
add, delete, and update the topic list. In addition, it uses:
1) Java 8 NIO methods
2) String operations
3) Stream operations
4) IntStream functions
5) Functional interface
6) Lambda functions
7) Optional datatype
8) Foreach loops
9) Default and Static methods in interface
10) Java 8 LocalDateTime API
11) Pattern

## Prerequisites

1) Java 17+ (JDK)
2) Maven (or use the bundled `./mvnw` wrapper)
3) POSTMAN / curl to call the APIs

## Getting Started

Run in Quarkus dev mode (live reload):
```
./mvnw quarkus:dev
```

Build the runnable application:
```
./mvnw package
```
This produces the runner under `target/quarkus-app/`. Run it with:
```
java -jar target/quarkus-app/quarkus-run.jar
```

The application listens on `localhost:8080`.

## Available APIs

Greetings
```
GET /
```
Get all Topics in List
```
GET /topic
```
Get Topic of given ID
```
GET /topic/{id}
```
Add Topic in List
```
POST /topic
```
Update Topic of given ID
```
PUT /topic/{id}
```
Delete Topic of given ID
```
DELETE /topic/{id}
```
Get all Topics whose ID's length is greater than minLength
```
GET /topic/minimum/length/{minLength}
```
Get all Topics sorted by ID
```
GET /topic/sort
```
String Operations on Topic List
```
GET /topic/string/operation
```
File Operations on Topic List
```
GET /topic/file/operation
```
Java 8 Date Time example
```
GET /datetime
```

## Helpful Links

Quarkus:

https://quarkus.io/guides/

https://quarkus.io/guides/rest

https://quarkus.io/guides/datasource

Java 8:

https://www.baeldung.com/java-8-functional-interfaces

https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html

https://docs.oracle.com/javase/tutorial/essential/io/pathOps.html

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [Quarkus](https://quarkus.io/) - Supersonic Subatomic Java framework

## Authors

* **Rehman Murad Ali**
