# Test Generation Workflow — springboot-java8

> Hand-off prompt for a Devin session. The session acts as the **parent/orchestrator**: it prepares the build for testing, fans out one child session per work unit with a dynamic workflow (`run_workflow`) to **write** tests in parallel, then integrates and verifies everything on a single branch.

---

## Goal

`COG-GTM/springboot-java8` has no tests: there is no `src/test/` directory and no test dependencies in `pom.xml`. Generate a JUnit test suite covering every production class under `src/main/java/hello/`, wire up coverage reporting, and deliver it as one PR with a coverage summary.

## Repository facts (verified against `master` — re-check before acting)

| Area | State |
| --- | --- |
| Production code | 13 classes under `src/main/java/hello/` in `controller/`, `service/`, `model/`, `declaration/`, plus `Application.java` |
| Tests | None. `src/test/java/` does not exist |
| Stack | Java 8 (`java.version` 1.8), Spring Boot **2.0.2.RELEASE** |
| `pom.xml` | `<packaging>pom</packaging>` — Maven will **not** compile `src/main` or run tests until this is `jar`. No `spring-boot-starter-test`, no JaCoCo |
| `build.gradle` | Gradle wrapper **4.6**, `compile`/`testCompile` configurations. Declares only `spring-boot-starter-web` + `junit:junit`; it is missing `spring-boot-starter-jdbc` and `h2`, so `./gradlew compileJava` currently **fails** on `JdbcTemplate` in `Application.java` |
| Wrappers | `gradlew` and `mvnw` are committed without the executable bit (mode `100644`) — call them via `bash ./gradlew` / `bash ./mvnw` or fix the mode with `git update-index --chmod=+x` |

### Environment gotchas

- **Run the build on JDK 8.** Spring Boot 2.0.2's `spring-boot-starter-test` ships JUnit **4.12** and Mockito 2.15; on JDK 17 any `mock(...)` / `@MockBean` fails with `Could not find sun.misc.Unsafe`. Set `JAVA_HOME` to a JDK 8 install (e.g. `/usr/lib/jvm/java-8-openjdk-amd64`). Gradle 4.6 also does not run on JDK 11+.
- **Tests are JUnit 4**: `org.junit.Test`, `@RunWith(SpringRunner.class)`, `@Test(expected = ...)`. Do not add JUnit 5 / `@ExtendWith` unless you also upgrade Spring Boot (out of scope).
- If a machine-wide Gradle init script (in `~/.gradle/init.d/`) uses APIs newer than Gradle 4.6 and breaks the build, run Gradle with a clean `GRADLE_USER_HOME` that contains only a repository-mirror init script.
- `Application` registers a `CommandLineRunner` bean that calls `http://gturnquist-quoters.cfapps.io/api/random` (host is no longer live) and then calls `quote.toString()`. Any `@SpringBootTest` that loads the full context must `@MockBean RestTemplate` and stub `getForObject(..., Quote.class)` to return a `Quote`, otherwise context startup fails. Prefer `@WebMvcTest` slices for controllers.
- `TopicService` keeps topics in an **instance-level mutable list** and several methods mutate it (`addTopic`, `updateTopic`, `deleteTopic`, `sortTopicsWithID`). Use a fresh `new TopicService()` per test so tests are order-independent.
- `TopicService`'s file methods use paths relative to the working directory (`Paths.get("")`, `temp.txt` at the repo root). Maven Surefire and Gradle both run tests from the project root, so assertions can rely on `temp.txt` and the `gradle*` files existing there — do not move or edit those files.

---

## Phase 1 — Parent session: discovery & setup (serial)

1. **Inventory.** List every `.java` file under `src/main/java/` and record the fully-qualified class names. This listing — not the table above — is the source of truth for Phase 2.
2. **Test source root.** Confirm whether `src/test/java/` exists. If it does not, create `src/test/java/hello/` (add a `.gitkeep` only if the directory would otherwise be empty when committed).
3. **Build tooling — keep `pom.xml` and `build.gradle` in sync.**
   - `pom.xml`
     - Change `<packaging>pom</packaging>` to `<packaging>jar</packaging>`.
     - Add `org.springframework.boot:spring-boot-starter-test` with `<scope>test</scope>` (version managed by the Boot parent; brings JUnit 4, Mockito, AssertJ, Hamcrest, Spring Test).
     - Add `jacoco-maven-plugin` (a 0.8.x release that runs on JDK 8, e.g. `0.8.8`) with `prepare-agent` and a `report` execution bound to the `test` phase.
   - `build.gradle`
     - Add the dependencies the Maven build already has: `compile("org.springframework.boot:spring-boot-starter-jdbc")`, `compile("com.h2database:h2")`, `runtime("org.springframework.boot:spring-boot-properties-migrator")`.
     - Replace `testCompile("junit:junit")` with `testCompile("org.springframework.boot:spring-boot-starter-test")`.
     - `apply plugin: 'jacoco'`, set `jacoco { toolVersion = "<same version as Maven>" }`, and make `test` finalized by `jacocoTestReport` with XML + HTML reports enabled.
   - Make the wrappers executable (`git update-index --chmod=+x gradlew mvnw`).
4. **Prove the setup.** On JDK 8, add one throwaway smoke test (e.g. `new TopicService().getAllTopics()` has 3 entries), then run both `mvn -B test` and `./gradlew test`. Both must compile and pass, and both must produce a JaCoCo report. Delete the smoke test afterwards.
5. **Publish the base branch.** Commit the build changes on a new branch (e.g. `devin/<timestamp>-generate-test-suite`) and **push it**. Child sessions run on their own machines and can only start from pushed code.
6. **Group work units.** Compute units from the Phase 1 listing: **one unit per top-level package** under `hello/`, splitting a package into additional units if it has more than ~6 classes. Classes directly in `hello/` join the smallest related unit. With today's code this yields:

   | Unit | Scope | Classes |
   | --- | --- | --- |
   | A — controllers | `hello/controller/` | `GreetingController`, `HelloController`, `TopicController` |
   | B — service | `hello/service/` | `TopicService` |
   | C — model | `hello/model/` | `Customer`, `Greeting`, `Quote`, `SimpleTimeClient`, `Topic`, `Value` |
   | D — declaration + app | `hello/declaration/`, `hello/Application.java` | `CustomPredicate`, `TimeClient`, `Application` |

   If the listing differs from this table, use the listing.

---

## Phase 2 — Dynamic fan-out: one child session per work unit (parallel)

Run a dynamic workflow with `run_workflow`. Each unit becomes one `agent()` call; all units run concurrently (`pipeline` over the unit list — there is no barrier between children).

**Workflow design**

- Build the unit list deterministically inside the workflow script by walking `src/main/java/hello/` (same grouping rule as Phase 1 step 6), so the fan-out is reproducible and a resumed run replays completed units.
- Agents run as **separate-VM** child sessions (`repos=["COG-GTM/springboot-java8"]`); the base branch is pushed, so nothing needs to be shared from the parent's machine.
- Each child owns a **disjoint set of test directories** (`src/test/java/hello/<package>/` for its unit only) and must **not** edit `pom.xml`, `build.gradle`, or anything under `src/main/`. That keeps child branches conflict-free. If a child believes it needs a dependency or a production change, it reports it instead of making it.
- Set `soft_time_limit_minutes` to ~30 per child.
- On `WorkflowAgentError`, retry the unit once, then record it as failed and continue; Phase 3 reports failed units rather than hiding them.

**Child prompt** (fill in `<BASE_BRANCH>`, `<UNIT>`, `<PACKAGES>`, `<CLASSES>`):

```text
Repository: COG-GTM/springboot-java8. Check out <BASE_BRANCH> (already has spring-boot-starter-test
and JaCoCo configured in both pom.xml and build.gradle). Create branch <BASE_BRANCH>-unit-<UNIT>.

Your scope is ONLY these production classes: <CLASSES> (packages: <PACKAGES>).
Write JUnit 4 tests for them under src/test/java/hello/<package>/, mirroring the source package.
Name each test class <ClassName>Test.

Requirements:
- For every public method: happy-path tests plus edge/error cases — null inputs, empty collections,
  not-found lookups, boundary values, and exceptions.
- Controllers: @RunWith(SpringRunner.class) + @WebMvcTest(<Controller>.class) with MockMvc and
  @MockBean for TopicService. Assert status codes, JSON bodies, and service interactions (Mockito verify).
- Services: plain unit tests on a fresh `new TopicService()` per test (the service holds mutable state).
- Full-context tests (@SpringBootTest) must @MockBean RestTemplate and stub getForObject(..., Quote.class)
  to return a Quote, because Application's CommandLineRunner calls an external host that is offline.
- Use JUnit 4 (org.junit.Test, @Test(expected=...)), Mockito, AssertJ/Hamcrest — all provided by
  spring-boot-starter-test. Do not add dependencies.
- Use JDK 8 (JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 or equivalent). Mockito fails on JDK 17
  with this Spring Boot version.

Rules:
- Do NOT modify pom.xml, build.gradle, or anything under src/main/. Do NOT touch test packages
  outside your scope.
- If a test exposes a genuine bug, keep a test that documents the CURRENT behavior so the suite stays
  green, mark it with a `// BUG:` comment explaining the expected behavior, and report it.
  Do not fix production code.
- Before finishing, run `mvn -B test` on JDK 8. All tests must compile and pass. Report your tests'
  line coverage for your classes from target/site/jacoco/jacoco.csv.
- Commit with a message containing the word "feature", push your branch, and do NOT open a PR.
```

**Structured output schema for each child**

```json
{
  "type": "object",
  "properties": {
    "unit": {"type": "string"},
    "branch": {"type": "string"},
    "test_files": {"type": "array", "items": {"type": "string"}},
    "tests_added": {"type": "integer"},
    "tests_per_class": {"type": "object", "additionalProperties": {"type": "integer"}},
    "all_tests_pass": {"type": "boolean"},
    "line_coverage_by_class": {"type": "object", "additionalProperties": {"type": "number"}},
    "bugs_found": {"type": "array", "items": {"type": "string"}},
    "requested_changes": {"type": "array", "items": {"type": "string"}},
    "notes": {"type": "string"}
  },
  "required": ["unit", "branch", "test_files", "tests_added", "all_tests_pass", "bugs_found"]
}
```

**Unit-specific coverage the children must include**

- **Unit A — controllers**
  - `TopicController`: every endpoint (`GET /topic`, `GET /topic/{id}`, `POST /topic`, `PUT /topic/{id}`, `DELETE /topic/{id}`, `GET /topic/minimum/length/{minLength}`, `GET /topic/sort`), including `GET /topic/{id}` when the service throws `NoSuchElementException`, malformed JSON bodies on POST/PUT, and a non-numeric `minLength`.
  - `GreetingController`: default `name=World`, custom name, and that the id increments across calls.
  - `HelloController`: `/datetime`, `/topic/string/operation`, `/topic/file/operation` with the service mocked — assert each response contains every section template and the mocked values in order.
- **Unit B — service** (`TopicService`)
  - `getTopicWithId`: found, and **not found → `NoSuchElementException` from `Optional.get()`** (lines 38–39), plus `null` id.
  - `updateTopic` / `deleteTopic`: existing id, missing id (no-op), `null` id; `addTopic` with a duplicate id.
  - `filterMinimumLengthForId`: 0, boundary (equal to an id length — strict `>`), larger than every id; `null` → exception.
  - `sortTopicsWithID` (mutates the backing list), `returnAllTopicIDWithStringSlicing`, `makeDistinctAndSortCharacters` (incl. empty string), `splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin` (no matches), `findIdHavingCharacter`.
  - File methods: `findAllFilesInPathAndSort`, `findParticularFileInPathAndSort`, `findParticularFileInPathAndSortWithWalkFunction` (assert `gradlew`/`gradle` entries appear), `readFileWithStreamFunction` (assert exact output from the committed `temp.txt`).
- **Unit C — model**
  - Constructors and accessors for all models; `toString()` for `Customer`, `Quote`, `Value`; Jackson round-trip for `Topic` and `Quote`/`Value`; JSON serialization for `Greeting` (it has no default constructor or setters, so deserialization is not supported).
  - `SimpleTimeClient`: `setTime`, `setDate`, `setDateAndTime`, `getLocalDateTime`, `toString`, and invalid values (e.g. hour 25 → `DateTimeException`).
- **Unit D — declaration + app**
  - `TimeClient.getZoneId`: valid zone, invalid zone falls back to `ZoneId.systemDefault()`; default `getZonedDateTime` via `SimpleTimeClient` or a minimal anonymous `TimeClient` implementation.
  - `CustomPredicate`: lambda implementation true/false cases.
  - `Application`: context loads with `RestTemplate` mocked; `run(...)` creates the `customers` table in H2 and inserts 4 rows (query via `JdbcTemplate`); `restTemplate(...)` bean is created.

### Known bug candidates to verify and flag (do not fix)

| Location | Suspected issue |
| --- | --- |
| `TopicService.getTopicWithId` | Unknown id throws `NoSuchElementException`; `GET /topic/{id}` returns HTTP 500 instead of 404 |
| `TopicService.updateTopic` / `deleteTopic` | `null` id (or a stored topic with `null` id) throws `NullPointerException` |
| `SimpleTimeClient.setDate` / `setDateAndTime` | Calls `LocalDate.of(day, month, year)`; `LocalDate.of` expects `(year, month, day)`, so normal dates throw `DateTimeException` |
| `Application` | Startup depends on an offline external quote service; the `CommandLineRunner` NPEs if the call returns `null` |

---

## Phase 3 — Parent session: integrate & verify

1. **Merge.** From the base branch, merge every child branch reported in the workflow results. Branches touch disjoint test directories, so conflicts indicate a scope violation — resolve them and note it.
2. **Apply requested changes.** Review each child's `requested_changes`; apply build-file changes to **both** `pom.xml` and `build.gradle`. Production-code changes stay out of scope.
3. **Run the full suite on JDK 8** with both builds: `mvn -B clean test` and `./gradlew clean test`. Fix integration or compilation issues (duplicate class names, shared fixtures, test-order dependence) until both are green.
4. **Coverage.** Generate the JaCoCo reports (`target/site/jacoco/`, `build/reports/jacoco/test/`) and read overall plus per-class line/branch coverage.
5. **Summary.** Write `TEST_GENERATION_SUMMARY.md` at the repo root containing:
   - Tests added per class (test class, test count, notable cases).
   - Line and branch coverage per class and overall.
   - Bugs found — a separate section, with the `// BUG:` test that documents each one.
   - Remaining gaps (uncovered lines/branches and why), failed or retried units, and follow-ups.
   - A per-unit table: unit, child session, branch, tests added, pass/fail.
6. **PR.** Open one PR from the base branch with the build changes, the generated tests, and `TEST_GENERATION_SUMMARY.md`. Include the commands used to run the suite and the overall coverage number in the description.

---

## Constraints

- Do not modify production logic under `src/main/`. When a test reveals a genuine bug, document it with a test and in the summary's bug section; fixes go in separate follow-up PRs.
- Every dependency or plugin change must be made in **both** `pom.xml` and `build.gradle`.
- Stay on Java 8 / Spring Boot 2.0.2 — no framework or language upgrades as part of this work.
- Tests must be deterministic: no network calls, no dependence on wall-clock dates beyond relative assertions, no reliance on test execution order.
