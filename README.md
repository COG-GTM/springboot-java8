# springboot-java8
The project is made on Spring Boot. It summarizes the new features introduced in Java 8,
now demonstrated on a modern runtime: **Java 21** and **Spring Boot 3.5.x**.
It contains a hardcoded list of topics. You can call the APIs (e.g. with POSTMAN or curl) to add, delete, and update the topic list.
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
1) Clone the project:
```
git clone https://github.com/COG-GTM/springboot-java8.git
```
2) Build and run with the Maven wrapper:
```
./mvnw spring-boot:run
```
   or with the Gradle wrapper:
```
./gradlew bootRun
```
The application starts on `http://localhost:8080`.

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

1) Java 21 (JDK 21)
2) Maven 3.9+ or Gradle 8.x (wrappers are included, so a local install is optional)
3) POSTMAN or curl (optional, to call the APIs)

### Installing

```
1) Clone the repository
2) Build/run with ./mvnw spring-boot:run (or ./gradlew bootRun)
3) Open Postman or curl to call the APIs ( localhost:8080 )
```

### Running the tests

```
./mvnw test
```
or
```
./gradlew test
```


## Helpful Links
Spring:

https://spring.io/guides

Java 8: 

https://www.baeldung.com/java-8-functional-interfaces

http://winterbe.com/posts/2015/05/22/java8-concurrency-tutorial-atomic-concurrent-map-examples/

https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html

https://docs.oracle.com/javase/tutorial/essential/io/pathOps.html

https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html

https://www.baeldung.com/foreach-java

http://winterbe.com/posts/2015/03/25/java8-examples-string-number-math-files/

https://www.baeldung.com/java-8-comparator-comparing

https://www.baeldung.com/java-8-sort-lambda

https://dzone.com/articles/java-8-friday-goodies-new-new


## Built With

* [Java 21](https://www.oracle.com/java/) - Language / runtime
* [Spring Boot 3.5.x](https://spring.io/projects/spring-boot) - Application framework
* [Maven](https://maven.apache.org/) / [Gradle](https://gradle.org/) - Build & dependency management

## Authors

* **Rehman Murad Ali** 


