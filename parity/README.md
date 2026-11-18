# REST parity harness

Proves that a build answers every REST endpoint exactly like the Java 8 / Spring Boot 2.0.2
baseline. Python 3 standard library only.

- `scenarios.json`: 69 requests in 4 scenarios (read-only, CRUD, request errors, a topic with a null id).
  Each scenario runs in a fresh JVM and a fresh working directory, so CRUD state and file
  operations never leak between scenarios.
- `snapshots/baseline-java8-boot2.0.json`: responses captured from the Java 8 / Boot 2.0.2 build.
- `snapshots/candidate-java21-boot3.3.json`: responses captured from the Java 21 / Boot 3.3 build.
- `accepted-differences.json`: reviewed differences that do not fail the comparison. Every
  instance is still listed in the report; anything not matched by a rule fails.
- `REPORT.md`: the latest comparison.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw -B package   # or ./gradlew bootJar
python3 parity/parity.py capture --jar target/gs-spring-boot-0.1.0.jar \
    --java-home /usr/lib/jvm/java-21-openjdk-amd64 \
    --label "Java 21 / Spring Boot 3.3.13 (candidate)" \
    --out parity/snapshots/candidate-java21-boot3.3.json
python3 parity/parity.py compare parity/snapshots/baseline-java8-boot2.0.json \
    parity/snapshots/candidate-java21-boot3.3.json --report parity/REPORT.md
```

`compare` exits non-zero on any unexplained difference, uncovered endpoint, or change in the
endpoint inventory. Pass `--accepted ''` to see the raw differences.

To re-capture the baseline, check out the commit that adds this harness (Java 8 / Boot 2.0.2),
build it with JDK 8 and run `capture` with `--java-home` pointing at JDK 8.
