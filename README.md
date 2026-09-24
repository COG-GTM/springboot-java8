# springboot-java8
Spring Boot 3.3.13 application running on Java 21. The project summarizes the modern Java feature set that started with Java 8.
It contain list of harcoded topics list. You can call the apis's with POSTMAN to add,delete,update Topic list
In addition, it uses 
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



## Getting Started
1) Download or clone the project with link
(https://github.com/RehmanMuradAli/springboot-java8/)

## Build and Run

Requires JDK 21 (`JAVA_HOME` must point at a Java 21 installation).

Maven:
```
./mvnw clean verify      # compile + run the test suite
./mvnw spring-boot:run   # run the app on http://localhost:8080
```

Gradle:
```
./gradlew test           # run the test suite
./gradlew bootRun        # run the app on http://localhost:8080
```

## Available API's

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



### Prerequisites

1) JDK 21
2) POSTMAN (optional, for calling the API's)

## Upgrade notes (Spring Boot 2.0.2 / Java 8 -> Spring Boot 3.3.13 / Java 21)

Endpoint paths and responses are unchanged. Behavioral deltas introduced by the upgrade:

* The startup quote fetch is now opt-in. It only runs when `app.quote.enabled=true`
  (default `false`), is configurable via `app.quote.url` plus connect/read timeouts, and
  logs a warning instead of failing startup when the remote host is unreachable.
* `application.properties` moved onto the classpath at `src/main/resources`, so the H2
  datasource settings are picked up by both the Maven and Gradle builds.
* `spring-boot-properties-migrator` was removed; it is a Spring Boot 2.x-only helper.

### Installing



```
1) Download or clone
2) Import the project
3) Run on location machine
4) Open Postman, to call API's (  localhost:8080 )
```


## Helpful Links
Spring:

https://spring.io/guides

Java 8: 

http://www.baeldung.com/java-8-functional-interfaces

http://winterbe.com/posts/2015/05/22/java8-concurrency-tutorial-atomic-concurrent-map-examples/

https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html

http://winterbe.com/posts/2014/07/31/java8-stream-tutorial-examples/

http://www.oracle.com/technetwork/articles/java/ma14-java-se-8-streams-2177646.html

https://docs.oracle.com/javase/tutorial/essential/io/pathOps.html

https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html

http://www.baeldung.com/foreach-java

http://winterbe.com/posts/2015/03/25/java8-examples-string-number-math-files/

http://www.baeldung.com/java-8-comparator-comparing

http://www.baeldung.com/java-8-sort-lambda

https://dzone.com/articles/java-8-friday-goodies-new-new


## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Rehman Murad Ali** 


