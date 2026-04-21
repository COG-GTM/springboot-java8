# springboot-java-modern (formerly springboot-java8)
Spring Boot REST service showcasing modern Java APIs. Originally a Java 8 demo,
upgraded to **Java 25 (LTS)** and **Spring Boot 3.4** with a batch of quality
improvements.

The service exposes a CRUD API over an in-memory list of topics plus endpoints
that demonstrate stream, file, string, and date-time APIs. Call the endpoints
with `curl`, Postman, or the bundled Swagger UI.

Language/library features demonstrated:

1) NIO (`java.nio.file`) streams
2) String operations
3) `Stream` operations
4) `IntStream`
5) Functional interfaces
6) Lambdas
7) `Optional`
8) `forEach`
9) Default & static methods on interfaces
10) `java.time` (LocalDateTime, ZonedDateTime)
11) `Pattern`
12) Records (Java 16+)
13) Text blocks (Java 15+)
14) `String.formatted` (Java 15+)
15) `Stream#toList` (Java 16+)
16) `Pattern#asMatchPredicate` (Java 11+)



## Getting Started

```bash
# build + test
./mvnw verify

# run
./mvnw spring-boot:run
```

Then visit:
- `http://localhost:8080/` — Greeting endpoint
- `http://localhost:8080/swagger-ui.html` — OpenAPI UI
- `http://localhost:8080/actuator/health` — Health check

## Available APIs

Greeting
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

1) Java sdk
2) POSTMAN

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


