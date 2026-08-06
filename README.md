# springboot-java8

A small Spring Boot REST service whose purpose is to **demonstrate Java language features** — originally the Java 8
feature set, which is still what the code shows off. The name stuck, but the project itself now builds and runs on
**Java 21 with Spring Boot 3.5.16**.

The domain is deliberately trivial: an in-memory, hardcoded list of `Topic` objects that you can create, read, update
and delete over HTTP, plus a few endpoints that exist purely to print the result of stream / string / file operations.

## Java 8 features demonstrated

1. NIO.2 file APIs (`Files.list`, `Files.find`, `Files.walk`, `Files.newBufferedReader`)
2. String operations (`String.join`, `String.chars`)
3. Stream operations (`filter`, `map`, `sorted`, `Collectors.joining`)
4. `IntStream` (`IntStream.range` for index lookups)
5. Functional interfaces (`CustomPredicate<T>`)
6. Lambda expressions
7. `Optional` / `OptionalInt`
8. `forEach` loops
9. `default` and `static` methods on interfaces (`TimeClient`)
10. The `java.time` API (`LocalDateTime`, `ZonedDateTime`, `ChronoUnit`)
11. `Pattern` as a stream source and as a predicate (`splitAsStream`, `asPredicate`)
12. try-with-resources over streams of paths

## Java 21 idioms in use

The application was migrated from Spring Boot 2.0.2 / Java 8 to Spring Boot 3.5.16 / Java 21, and the presentation
layer has been rewritten to use the modern language features that replace what the old code did by hand:

- **Text blocks** (Java 15) for the multi-part responses of `/datetime`, `/topic/string/operation` and
  `/topic/file/operation`, using `\` line-continuation escapes so the responses stay on a single line.
- **`String.formatted(...)`** (Java 15) instead of `String.format(...)` and `+` concatenation.
- Label templates are `private static final` constants rather than mutable instance fields.

## Requirements

- JDK 21 (the build sets `<java.version>21</java.version>`)
- Maven is **not** required — use the bundled Maven Wrapper (`./mvnw`, Maven 3.9.9)
- `curl`, HTTPie or Postman to call the API

## Getting started

```bash
git clone https://github.com/COG-GTM/springboot-java8.git
cd springboot-java8

# compile, run the test suite and package the jar
./mvnw clean verify

# run the application (listens on http://localhost:8080)
./mvnw spring-boot:run
```

You can also run the packaged jar directly:

```bash
java -jar target/gs-spring-boot-0.1.0.jar
```

> The Gradle build has been removed; Maven is the only supported build.

Every push and pull request is built by GitHub Actions with `./mvnw -B clean verify` on JDK 21
(see `.github/workflows/ci.yml`).

### Expected startup noise

- On startup the app creates an in-memory **H2** `customers` table with `JdbcTemplate` and logs a few rows — this is
  part of the demo, not an error.
- It also tries to fetch a random quote from `gturnquist-quoters.cfapps.io`, a demo service that no longer exists.
  The resulting `WARN Could not fetch a quote from ...` is expected and must never fail startup.

## API

All endpoints are served from `http://localhost:8080`.

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/` | Greeting; accepts an optional `?name=` parameter (defaults to `World`) |
| `GET` | `/topic` | All topics |
| `GET` | `/topic/{id}` | A single topic by id |
| `POST` | `/topic` | Add a topic (JSON body) |
| `PUT` | `/topic/{id}` | Replace the topic with the given id |
| `DELETE` | `/topic/{id}` | Delete the topic with the given id |
| `GET` | `/topic/sort` | All topics sorted by id |
| `GET` | `/topic/minimum/length/{minLength}` | Topics whose id is longer than `minLength` |
| `GET` | `/topic/string/operation` | Plain-text dump of the string/stream/regex examples |
| `GET` | `/topic/file/operation` | Plain-text dump of the NIO.2 file examples |
| `GET` | `/datetime` | Plain-text dump of the `java.time` examples |

The topic list lives in memory, so `POST` / `PUT` / `DELETE` changes are lost when the application restarts.

### Examples

```bash
$ curl 'http://localhost:8080/?name=Devin'
{"id":1,"content":"Hello, Devin!"}

$ curl http://localhost:8080/topic
[{"id":"spring","subjectName":"Spring Framework","subjectDescription":"Spring Framework Description"}, ...]

$ curl -X POST http://localhost:8080/topic \
    -H 'Content-Type: application/json' \
    -d '{"id":"kotlin","subjectName":"Kotlin","subjectDescription":"Kotlin Description"}'
```

The `/topic/file/operation` endpoint resolves paths relative to the working directory the application was started
from, so its output depends on where you run it.

## Project layout

```
src/main/java/hello
├── Application.java          # entry point + CommandLineRunner (H2 seeding, quote fetch)
├── config/                   # RestTemplate bean
├── controller/               # GreetingController, TopicController, HelloController
├── declaration/              # CustomPredicate, TimeClient (default/static interface methods)
├── model/                    # Topic, Greeting, Customer, Quote, Value, SimpleTimeClient
└── service/                  # TopicService — where most of the Java 8 examples live
```

## Helpful links

Spring:

https://spring.io/guides

Java 8:

http://www.baeldung.com/java-8-functional-interfaces

https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html

http://winterbe.com/posts/2014/07/31/java8-stream-tutorial-examples/

https://docs.oracle.com/javase/tutorial/essential/io/pathOps.html

Java 21:

https://docs.oracle.com/en/java/javase/21/text-blocks/index.html

https://docs.spring.io/spring-boot/docs/current/reference/html/

## Built with

* [Maven](https://maven.apache.org/) — build and dependency management
* [Spring Boot](https://spring.io/projects/spring-boot) 3.5.16

## Authors

* **Rehman Murad Ali** — original author
