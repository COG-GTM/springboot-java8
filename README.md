# springboot-java8
A Spring Boot project demonstrating Java features including streams, lambdas, NIO, and more.
It contains a list of hardcoded topics. You can call the APIs with POSTMAN to add, delete, update the Topic list.
In addition, it uses:
1) Java NIO methods
2) String operations
3) Stream operations
4) IntStream functions
5) Functional interface
6) Lambda functions
7) Optional datatype
8) Foreach loops
9) Default and Static methods in interface
10) Java LocalDateTime API
11) Pattern



## Requirements

- **Java 21**
- **Spring Boot 3.4.5**
- **Maven 3.9.9** (via Maven Wrapper) or **Gradle 8.12** (via Gradle Wrapper)

## Getting Started

### Build & Run with Maven
```bash
./mvnw clean package
java -jar target/gs-spring-boot-0.1.0.jar
```

### Build & Run with Gradle
```bash
./gradlew build
java -jar build/libs/gs-spring-boot-0.1.0.jar
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
Java Date Time example
```
GET /datetime
```



## Helpful Links
Spring:

https://spring.io/guides

Java:

http://www.baeldung.com/java-8-functional-interfaces

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
* [Spring Boot](https://spring.io/projects/spring-boot) - Application Framework

## Authors

* **Rehman Murad Ali**
