# Migration Log: Java 8 → Java 21, Spring Boot 2.0.2 → 3.5.x

This document tracks every change made while migrating the application from
Java 8 / Spring Boot 2.0.2.RELEASE to Java 21 / Spring Boot 3.5.0 using a
spec-driven approach (tests written first, then migration changes verified
against those tests after each step).

## Change Log

### 2026-06-23 - Step 0: Create MIGRATION_LOG.md
- Created this living document to track every migration change.
- Files modified: `MIGRATION_LOG.md` (new)
- Status: PASS

### 2026-06-23 - Step 1: Write specs (tests) for existing behavior
- Added `spring-boot-starter-test` (test scope) to `pom.xml` (still on Spring Boot 2.0.2).
- Created `src/test/java/hello/` with `@SpringBootTest`/`MockMvc` integration tests and plain unit tests:
  - `controller/TopicControllerTest.java` — GET all (3 defaults), GET by id, POST, PUT, DELETE,
    `/topic/minimum/length/4`, `/topic/sort`
  - `controller/GreetingControllerTest.java` — default `World` and named greeting
  - `controller/HelloControllerTest.java` — `/datetime`, `/topic/string/operation`, `/topic/file/operation`
  - `service/TopicServiceTest.java` — all `TopicService` methods (CRUD, filter, sort, string slicing,
    distinct/sort chars, java-keyword filter, find id having 'g')
  - `model/SimpleTimeClientTest.java` — `setTime`/`setDate`/`setDateAndTime`/`getLocalDateTime`/`getZonedDateTime`
  - `declaration/CustomPredicateTest.java` — functional interface via lambda
- The dead external URL (`http://gturnquist-quoters.cfapps.io/api/random`) is hit by the
  `CommandLineRunner run(RestTemplate)` bean at context startup. To let `@SpringBootTest` load, that
  bean was guarded with `@Profile("!test")` and the integration tests run with `@ActiveProfiles("test")`
  (the endorsed "test profile disables the CommandLineRunner" approach). The H2 `customers` table-creation
  `CommandLineRunner` still runs and is exercised by the integration tests.
- Tests written in JUnit 4 to match Spring Boot 2.0.2's bundled test stack (converted to JUnit 5 later, see post-Step 9).
- Result: `./mvnw test` → **30 tests, 0 failures** on Spring Boot 2.0.2 / Java 8.
- Files modified: `pom.xml`, `src/main/java/hello/Application.java`, 6 new test files.
- Status: PASS

### 2026-06-23 - Step 2: Fix packaging type in pom.xml
- Changed `<packaging>pom</packaging>` → `<packaging>jar</packaging>`.
  (`pom` packaging skips the compile/test lifecycle, so this was required for the baseline test run
  in Step 1 to actually execute.)
- Files modified: `pom.xml`
- Status: PASS (baseline still 30/30)

### 2026-06-23 - Step 3: Upgrade pom.xml to Spring Boot 3.5.0 and Java 21
- Parent `spring-boot-starter-parent` `2.0.2.RELEASE` → `3.5.0`.
- `<java.version>` `1.8` → `21`.
- Removed the `spring-boot-properties-migrator` dependency (only needed for the 1.x→2.x migration).
- Confirmed `spring-boot-starter-test` is present (added in Step 1).
- Updated `.mvn/wrapper/maven-wrapper.properties` distributionUrl to Maven `3.9.9`.
- `./mvnw clean compile` → **BUILD SUCCESS** (only a deprecation warning for the `JdbcTemplate.query`
  overload fixed in Step 7; no hard compilation errors).
- Files modified: `pom.xml`, `.mvn/wrapper/maven-wrapper.properties`
- Status: PASS

### 2026-06-23 - Step 4: Upgrade build.gradle to match
- Spring Boot Gradle plugin `2.0.2.RELEASE` → `3.5.0`.
- `sourceCompatibility`/`targetCompatibility` `1.8` → `21`.
- `compile(...)` → `implementation(...)`, `testCompile(...)` → `testImplementation(...)`.
- `bootJar { baseName / version }` → `archiveBaseName` / `archiveVersion`.
- Added `spring-boot-starter-test`, `spring-boot-starter-jdbc`, and `h2` (parity with `pom.xml`).
- Added `test { useJUnitPlatform() }` so JUnit 5 tests run under Gradle.
- Updated `gradle/wrapper/gradle-wrapper.properties` distributionUrl to Gradle `8.11`.
- `./gradlew clean test` → **BUILD SUCCESSFUL**, 30 tests pass.
- Files modified: `build.gradle`, `gradle/wrapper/gradle-wrapper.properties`
- Status: PASS

### 2026-06-23 - Step 5: Fix H2 SQL syntax
- `Application.java`: `DROP TABLE customers IF EXISTS` → `DROP TABLE IF EXISTS customers`
  (H2 2.x bundled with Spring Boot 3.x dropped the legacy syntax).
- Files modified: `src/main/java/hello/Application.java`
- Status: PASS

### 2026-06-23 - Step 6: Fix/remove dead external URL
- `Application.java`: removed the `RestTemplate` call to `http://gturnquist-quoters.cfapps.io/api/random`
  in `main()` and removed the `CommandLineRunner run(RestTemplate)` bean (the Cloud Foundry app is
  decommissioned). Kept the `RestTemplate` bean definition. Removed the now-unused `Quote` import
  (and the `@Profile`/import added in Step 1, which is no longer needed).
- `Quote`/`Value` model classes were left in place as Java-feature reference samples.
- Files modified: `src/main/java/hello/Application.java`
- Status: PASS

### 2026-06-23 - Step 7: Fix deprecated JdbcTemplate API
- `Application.java`: switched the customer query from
  `query(sql, new Object[]{"Josh"}, rowMapper)` to the varargs form
  `query(sql, rowMapper, "Josh")` (RowMapper before parameter values).
- Files modified: `src/main/java/hello/Application.java`
- Status: PASS

### 2026-06-23 - Step 8: Modernize controller annotations
- `TopicController.java`: `@RequestMapping` → `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping`.
- `GreetingController.java` and `HelloController.java`: `@RequestMapping` → `@GetMapping`.
- Files modified: `TopicController.java`, `GreetingController.java`, `HelloController.java`
- Status: PASS

### 2026-06-23 - Step 9: Move application.properties to standard location
- `git mv application.properties src/main/resources/application.properties`.
- Files modified: moved `application.properties`
- Status: PASS

### 2026-06-23 - Post-Step 9: Convert tests from JUnit 4 to JUnit 5
- Spring Boot 3.5's `spring-boot-starter-test` ships JUnit Jupiter (no JUnit 4 on the classpath),
  so the Step 1 tests were converted:
  - `org.junit.Test` → `org.junit.jupiter.api.Test`, `@Before` → `@BeforeEach`,
    `org.junit.Assert.*` → `org.junit.jupiter.api.Assertions.*`.
  - Removed `@RunWith(SpringRunner.class)` (JUnit 5 `@SpringBootTest` registers `SpringExtension`).
- Files modified: all 6 test files
- Status: PASS

### 2026-06-23 - Step 10: Run full test suite and verify
- `./mvnw clean test` → **30 tests, 0 failures** (Spring Boot 3.5.0 / Java 21).
- `./gradlew clean test` → **BUILD SUCCESSFUL**, 30 tests pass.
- `./mvnw spring-boot:run` → app starts on port 8080; H2 `customers` table is created and seeded
  on startup; smoke-tested every endpoint (`/`, `/topic`, `/topic/{id}`, POST/PUT/DELETE `/topic`,
  `/topic/sort`, `/topic/minimum/length/{n}`, `/datetime`, `/topic/string/operation`,
  `/topic/file/operation`) — all respond correctly. No dead-URL errors at startup.
- Status: PASS

### 2026-06-23 - Step 11: Update README.md
- Documented Java 21 + Spring Boot 3.5.x, updated Prerequisites, build/run instructions
  (`./mvnw spring-boot:run` / `./gradlew bootRun`), added a "Running the tests" section,
  refreshed the clone URL, removed a dead Oracle technetwork link, and upgraded several links to HTTPS.
- Files modified: `README.md`
- Status: PASS

### 2026-06-23 - Step 12: Final summary

**Dependency / toolchain version changes**
| Item | Before | After |
|------|--------|-------|
| Java | 1.8 | 21 |
| Spring Boot | 2.0.2.RELEASE | 3.5.0 |
| Maven wrapper | 3.3.9 | 3.9.9 |
| Gradle wrapper | 4.6 | 8.11 |
| Test framework | JUnit 4 | JUnit 5 (Jupiter) |
| `spring-boot-properties-migrator` | present | removed |
| `spring-boot-starter-test` | absent | added |

**Breaking changes fixed**
- `pom` → `jar` packaging.
- H2 `DROP TABLE customers IF EXISTS` → `DROP TABLE IF EXISTS customers`.
- Removed dead `gturnquist-quoters.cfapps.io` calls and its `CommandLineRunner`.
- `JdbcTemplate.query(sql, Object[], RowMapper)` → varargs `query(sql, RowMapper, args...)`.
- JUnit 4 → JUnit 5 test migration.
- `application.properties` moved to `src/main/resources/`.

**Files modified**
- `pom.xml`, `build.gradle`
- `.mvn/wrapper/maven-wrapper.properties`, `gradle/wrapper/gradle-wrapper.properties`
- `src/main/java/hello/Application.java`
- `src/main/java/hello/controller/{TopicController,GreetingController,HelloController}.java`
- `application.properties` → `src/main/resources/application.properties`
- `README.md`, `MIGRATION_LOG.md`
- New tests under `src/test/java/hello/`

**Test results**
- Maven: 30 tests, 0 failures, 0 errors.
- Gradle: 30 tests, 0 failures, 0 errors.
- Runtime smoke test: all endpoints OK; H2 seeding OK.

**Known issues / notes**
- Gradle 8.11 emits a deprecation warning that `sourceCompatibility`/`targetCompatibility` will be
  removed in Gradle 9 (use the `java { toolchain }` block when moving to Gradle 9). No functional impact today.
- `Quote`/`Value` models remain as Java-feature reference samples but are no longer wired to any endpoint.
- The legacy `.idea/` project files and `gs-spring-boot.iml` still reference old library versions; they
  are IDE artifacts and do not affect the build.

- Status: PASS — migration complete.
