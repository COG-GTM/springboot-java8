# Java 8 → Java 11 Migration Notes

This document summarizes the migration of `springboot-java8` from Java 8 to
**Java 11 (LTS)**. The goal was a minimal, behavior-preserving migration: upgrade
the build toolchain and CI to JDK 11, replace anything removed from the JDK, and
keep the application code unchanged.

## Summary

| Area | Before | After |
| --- | --- | --- |
| Java language/runtime target | 8 (`java.version=1.8`) | 11 (`maven.compiler.release=11`) |
| Maven packaging | `pom` (sources never compiled) | `jar` (sources compiled + repackaged) |
| Maven wrapper | 3.3.9 | 3.9.9 |
| maven-compiler-plugin | inherited 3.7.0 | 3.11.0, `<release>11</release>`, `-Xlint:all -Werror` |
| maven-surefire-plugin | inherited 2.21.0 | 3.2.5 |
| maven-failsafe-plugin | not configured | 3.2.5 |
| maven-javadoc-plugin | not configured | 3.6.3, doclint disabled |
| maven-enforcer-plugin | not configured | 3.5.0, `requireJavaVersion [11,)` |
| Gradle wrapper | 4.6 (does not run on JDK 11) | 6.9.4 |
| Gradle Java target | `sourceCompatibility = 1.8` | toolchain `languageVersion = 11` |
| CI | none | GitHub Actions, Temurin 11, Maven + Gradle |

## Baseline (Java 8)

A Java 8 baseline was captured before any changes:

- `JAVA_HOME` = Temurin/OpenJDK `1.8.0_492`
- `./mvnw -B clean package` → `BUILD SUCCESS`

Note: with the original `<packaging>pom</packaging>`, the Maven build never invoked
the compiler, so the "baseline" Maven build did not actually compile the `hello.*`
sources. The Gradle build was the only one that compiled code. This is called out
because it changes what "parity" means here (see below).

## Build tooling changes

### Maven (`pom.xml`)

- **`packaging` changed from `pom` to `jar`.** As a `pom` project the
  `maven-compiler-plugin` and `maven-surefire-plugin` never ran, so the Java 11
  `release` flag would have had no effect. Switching to `jar` makes Maven actually
  compile the sources under `--release 11` and produce a runnable Spring Boot jar
  via `spring-boot-maven-plugin:repackage`.
- Replaced `<java.version>1.8</java.version>` with
  `maven.compiler.release=11` plus UTF-8 source/reporting encoding.
- Added explicit, JDK-11-compatible plugin versions:
  `maven-compiler-plugin` 3.11.0 (`<release>11</release>`, `-Xlint:all`,
  `-Werror`), `maven-surefire-plugin` 3.2.5, `maven-failsafe-plugin` 3.2.5,
  `maven-javadoc-plugin` 3.6.3, `maven-enforcer-plugin` 3.5.0 enforcing
  `requireJavaVersion [11,)`.
- Added `spring-boot-starter-test` (test scope) so the project has a standard test
  source set; surefire reports `No tests to run` (the project ships no tests).
- Upgraded the Maven wrapper from 3.3.9 to 3.9.9 — the older plugin-aware tooling
  (surefire/enforcer 3.x) requires Maven 3.6.3+.

### Gradle (`build.gradle`)

- Upgraded the Gradle wrapper from 4.6 to 6.9.4. Gradle 4.6 cannot run on JDK 11.
- Replaced `sourceCompatibility/targetCompatibility = 1.8` with a Java
  **toolchain** pinned to `JavaLanguageVersion.of(11)`. (Gradle rejects combining a
  project-level toolchain with `source/targetCompatibility`, so the latter were
  removed.)
- Modernized the legacy `compile`/`testCompile` configurations to
  `implementation`/`testImplementation`.
- **Parity fix:** added `spring-boot-starter-jdbc` and `com.h2database:h2`. The
  original Gradle build only declared `spring-boot-starter-web`, so it failed to
  compile `hello.Application` (which uses `JdbcTemplate`). These deps already
  existed in `pom.xml`; adding them brings the Gradle build to parity with Maven.

## Removed JDK modules (Java EE / CORBA / JavaFX)

A scan of the source (`javax.xml.bind`, `javax.activation`, `javax.xml.ws`,
`com.sun.*`, `sun.misc.*`, Nashorn, `javafx.*`, CORBA / `org.omg.*`) found **no
usages**. No external replacement dependencies (JAXB, JAX-WS, JavaFX, Activation)
were required.

## Encapsulation / illegal reflective access

The application code triggers no illegal reflective access warnings when compiled
or packaged on JDK 11, so **no `--add-opens` flags were added**. The only reflective
access warning observed comes from Gradle 6.9.4's own Groovy runtime during the
build (not from application code); it disappears with a newer Gradle and is out of
scope for this behavior-preserving migration.

## Security / TLS, GC, logging

No code or configuration depends on TLS protocol/cipher pinning, keystore type, or
GC/JVM tuning flags (the only runtime config is the optional `server.port` in
`application.properties`). Java 11 defaults (TLS 1.3, PKCS12 keystores, G1 GC,
unified logging) therefore apply with no changes needed.

## Validation

- `./mvnw -B clean verify` on **JDK 11** → `BUILD SUCCESS` (compiles with
  `-Xlint:all -Werror`, enforcer confirms Java 11, jar repackaged).
- `./gradlew --no-daemon clean build` on **JDK 11** → `BUILD SUCCESSFUL`.
- No compiler warnings under `-Xlint:all`.
- Coverage is unchanged (the project ships no tests, so coverage is 0% before and
  after; CI is green on the build).

## Follow-ups (out of scope)

- Spring Boot 2.0.2 / Spring 5.0 predate official Java 11 support. Compilation and
  packaging are clean on JDK 11, but a future PR should upgrade to Spring Boot
  2.1+ (ideally 2.7.x) for fully supported Java 11 runtime behavior.
- Add an actual test suite (e.g. `@SpringBootTest`) so CI verifies runtime
  behavior, not just compilation.
- Resolve Gradle deprecation warnings by moving to Gradle 7/8 (requires the Spring
  Boot upgrade above, since the 2.0.2 Gradle plugin is incompatible with Gradle 7+).
