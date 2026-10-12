# springboot-java8
The project is built with Spring Boot. It summarizes the new features introduced in Java 8.
It contains a hardcoded list of topics. You can call the APIs with Postman (or curl) to add, delete and update topics in the list.
In addition, it uses:
1) Java 8 NIO methods
2) String operations
3) Stream operations
4) IntStream functions
5) Functional interfaces
6) Lambda functions
7) Optional datatype
8) forEach loops
9) Default and static methods in interfaces
10) Java 8 LocalDateTime API
11) Pattern

On startup, the application also runs a small JdbcTemplate demo against an in-memory H2 database
(creates a `customers` table, inserts a few rows and logs a query result).



## Getting Started
1) Download or clone the project from
(https://github.com/COG-GTM/springboot-java8)

```
git clone https://github.com/COG-GTM/springboot-java8.git
cd springboot-java8
```

## Available APIs

Greeting (returns JSON such as `{"id":1,"content":"Hello, World!"}`; optional `name` query parameter, e.g. `GET /?name=Java` returns `"Hello, Java!"`)
```
GET /
```
Get all Topics in List
```
GET /topic
```
Get Topic of given ID (returns HTTP 404 with Spring's default error JSON if the ID is unknown)
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

File Operations on Topic List (reads `temp.txt` relative to the working directory, so run the app from the project root)
```
GET /topic/file/operation
```
Java 8 Date Time example ("Time in California" is computed for the `America/Los_Angeles` zone)
```
GET /datetime
```

### Changes from the original project

* `GET /topic/{id}` now returns HTTP 404 for an unknown ID instead of failing with a server error.
* `/datetime` "Time in California" now uses `America/Los_Angeles` (previously `Canada/Central`) and converts the current instant to that zone.
* The external random-quote call (`gturnquist-quoters.cfapps.io`) was removed from startup; the service is no longer available and the application now starts without network access.



### Prerequisites

1) JDK 17
2) Postman or curl (optional, to call the APIs)

No global Maven or Gradle installation is needed; the project ships with the `./mvnw` and `./gradlew` wrappers.

### Installing

Build and run with Maven:

```
./mvnw package
java -jar target/gs-spring-boot-0.1.0.jar
```

Or run with Gradle:

```
./gradlew bootRun
```

(`./gradlew build` produces `build/libs/gs-spring-boot-0.1.0.jar`.)

The application starts on http://localhost:8080, e.g.:

```
curl http://localhost:8080/topic
```

### Running the tests

Tests use JUnit 5 (via `spring-boot-starter-test`) and run with either build tool:

```
./mvnw test
./gradlew test
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

* [Spring Boot 3.3.13](https://spring.io/projects/spring-boot)
* Java 17
* [Maven](https://maven.apache.org/) (wrapper `./mvnw`) and [Gradle](https://gradle.org/) (wrapper `./gradlew`) - Build and Dependency Management

## Authors

* **Rehman Murad Ali** - original project ([RehmanMuradAli/springboot-java8](https://github.com/RehmanMuradAli/springboot-java8))
* **COG-GTM** - maintainer of this fork
