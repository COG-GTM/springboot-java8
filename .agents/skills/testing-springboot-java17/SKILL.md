---
name: testing-springboot-java17
description: Build, run and end-to-end test the gs-spring-boot demo app (Java 17 / Spring Boot 3.x) on this box, including working around Maven Central rate limiting.
---

# Testing the springboot-java8 (now Java 17 / Spring Boot 3.x) app

## Build

- Java 17 is the default JDK (`java -version` -> 17.x). No toolchain install needed.
- **Maven**: `./mvnw` may fail — the wrapper script is often not executable (`Permission denied`) and
  downloading `apache-maven-*.zip` from `repo1.maven.org` can return **HTTP 429** (rate limited).
  Workaround: use the system Maven, which picks up the google mirror in `~/.m2/settings.xml`:
  `mvn clean package` -> `target/gs-spring-boot-0.1.0.jar` (boot-repackaged).
  If you must use the wrapper, run it as `sh mvnw` and expect the 429 unless a local dist is cached.
- **Gradle**: `build.gradle` declares `mavenCentral()`, which hits the same rate limit. Use an init
  script that swaps the repo for the Google mirror:
  ```groovy
  // /tmp/mirror.gradle
  allprojects {
      afterEvaluate {
          def kept = repositories.findAll { !(it.hasProperty('url') && it.url != null && it.url.host in ['repo.maven.apache.org','repo1.maven.org']) }
          repositories.clear()
          repositories.maven { url = 'https://maven-central.storage-download.googleapis.com/maven2' }
          kept.each { repositories.add(it) }
      }
  }
  ```
  Then `./gradlew clean build -I /tmp/mirror.gradle` -> `build/libs/gs-spring-boot-0.1.0.jar`.
  Note the init script only rewrites project repositories, not `buildscript {}` ones, so a cached
  Gradle dependency cache may still be required; if the buildscript block fails, add the same
  mirror inside `buildscript { repositories { ... } }` temporarily (do not commit).

## Run

`cd <repo> && java -jar target/gs-spring-boot-0.1.0.jar > /tmp/app.log 2>&1 &` — port 8080
(`application.properties` has `server.port` commented out). Run from the repo root: the
`/topic/file/operation` endpoint reads `temp.txt` from the process CWD.

Healthy startup log contains: `Starting Servlet engine: [Apache Tomcat/10.1.x]`, `Started Application`,
four `Inserting customer record for ...` lines and two `Customer{... firstName='Josh' ...}` lines
(proves the JdbcTemplate varargs `query(...)` overload works on Spring 6), plus a **WARN** (not an
error) for the unreachable `http://gturnquist-quoters.cfapps.io/api/random` quote URL — that domain
no longer resolves, so a WARN is expected behaviour.

## Endpoints (all `@RestController`, JSON/plain text, no auth)

| Route | Notes |
|---|---|
| `GET /` (`?name=X`) | GreetingController, counter increments each call |
| `GET /topic`, `GET /topic/{id}` | TopicController |
| `POST /topic`, `PUT /topic/{id}`, `DELETE /topic/{id}` | return 200 with empty body (void handlers) |
| `GET /topic/minimum/length/{n}` | ids with length **>** n |
| `GET /topic/sort` | sorts the in-memory list in place |
| `GET /datetime`, `GET /topic/string/operation`, `GET /topic/file/operation` | Java 8 stream/NIO demos |

State is a static in-memory list, so mutations persist until restart — restore state (delete what
you POSTed) or restart the jar between runs.

## Known pre-existing behaviour (not migration fallout)

- `GET /topic/{unknown-id}` -> **500** whitelabel error page; `TopicService.getTopicWithId` calls
  `Optional.get()` without a presence check. Expected 404 would need a code fix.
- `PUT`/`DELETE` on a non-existent id -> 200 no-op.

## Devin Secrets Needed

None — the app is fully local with an in-memory H2 database.
