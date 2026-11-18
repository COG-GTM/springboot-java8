# REST parity report: Java 8 / Spring Boot 2.0.2 (baseline) vs Java 21 / Spring Boot 3.3.13 (candidate)

**Result: PASS**

- 69 requests replayed in 4 scenarios: 56 identical, 13 accepted differences, 0 unexplained differences.
- 11/11 endpoints covered; endpoint inventory identical between the two builds.
- Startup JDBC output (customers query) identical in every scenario.

| | Baseline | Candidate |
|---|---|---|
| java | `openjdk version "1.8.0_504"` | `openjdk version "21.0.12.1" 2026-08-18` |
| spring_boot | `2.0.2.RELEASE` | `3.3.13` |
| build_jdk | `1.8.0_504` | `21` |
| git_sha | `d87c5ea` | `fd466ee` |
| captured_at | `2026-11-18T02:46:06+00:00` | `2026-11-18T02:49:48+00:00` |

Compared per request: HTTP status, `Content-Type`, `Allow`, and the full response body. Normalisation (both sides): error `timestamp` values are masked; ISO date-times are masked to `<DATETIME frac=N>` so the value is ignored but the format (fractional-second digits) is still compared. Each scenario runs in a fresh JVM with a fresh working directory (`scenarios.json` > `workdir`) and JVM options `-Duser.timezone=UTC -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8`.

## Endpoint coverage

Endpoints discovered statically from the controllers (`@RequestMapping` etc.); `ANY` means the mapping does not restrict the HTTP method.

| Method | Path | Controller | Requests covering it |
|---|---|---|---|
| ANY | `/` | GreetingController.java | 6 |
| ANY | `/datetime` | HelloController.java | 1 |
| ANY | `/topic` | TopicController.java | 19 |
| POST | `/topic` | TopicController.java | 9 |
| ANY | `/topic/file/operation` | HelloController.java | 1 |
| ANY | `/topic/minimum/length/{minLength}` | TopicController.java | 8 |
| ANY | `/topic/sort` | TopicController.java | 3 |
| ANY | `/topic/string/operation` | HelloController.java | 4 |
| ANY | `/topic/{id}` | TopicController.java | 25 |
| DELETE | `/topic/{id}` | TopicController.java | 3 |
| PUT | `/topic/{id}` | TopicController.java | 5 |

## Accepted differences

### `error-message-wording` (12 requests)

Only the human-readable `message` of a Spring Boot error response differs; status, `error`, `path` and the set of fields are identical. The wording comes from the framework and JDK (Spring 6 dropped 'nested exception is ...', Jackson 2.17 rephrased parse errors, Spring 6.1 reports 'No static resource ...' for unmapped paths, and JDK 14+ adds helpful NullPointerException messages).

- `read-only/min-length-not-a-number`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":400,"error":"Bad Request","message":"Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'; nested exception is java.lang.NumberFormatException: For input string: \"abc\"","path":"/topic/minimum/length/abc"}
+ {"timestamp":"<TIMESTAMP>","status":400,"error":"Bad Request","message":"Method parameter 'minLength': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'; For input string: \"abc\"","path":"/topic/minimum/length/abc"}
```
- `read-only/unknown-path`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":404,"error":"Not Found","message":"No message available","path":"/does-not-exist"}
+ {"timestamp":"<TIMESTAMP>","status":404,"error":"Not Found","message":"No static resource does-not-exist.","path":"/does-not-exist"}
```
- `request-errors/post-malformed-json`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":400,"error":"Bad Request","message":"JSON parse error: Unexpected end-of-input within/between Object entries; nested exception is com.fasterxml.jackson.core.io.JsonEOFException: Unexpected end-of-input within/between Object entries\n at [Source: (PushbackInputStream); line: 1, column: 15]","path":"/topic"}
+ {"timestamp":"<TIMESTAMP>","status":400,"error":"Bad Request","message":"JSON parse error: Unexpected end-of-input within/between Object entries","path":"/topic"}
```
- `request-errors/post-text-plain`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":415,"error":"Unsupported Media Type","message":"Content type 'text/plain;charset=UTF-8' not supported","path":"/topic"}
+ {"timestamp":"<TIMESTAMP>","status":415,"error":"Unsupported Media Type","message":"Content-Type 'text/plain;charset=UTF-8' is not supported.","path":"/topic"}
```
- `request-errors/post-json-array`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":400,"error":"Bad Request","message":"JSON parse error: Cannot deserialize instance of `hello.model.Topic` out of START_ARRAY token; nested exception is com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot deserialize instance of `hello.model.Topic` out of START_ARRAY token\n at [Source: (PushbackInputStream); line: 1, column: 1]","path":"/topic"}
+ {"timestamp":"<TIMESTAMP>","status":400,"error":"Bad Request","message":"JSON parse error: Cannot deserialize value of type `hello.model.Topic` from Array value (token `JsonToken.START_ARRAY`)","path":"/topic"}
```
- `request-errors/min-length-missing-segment`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":404,"error":"Not Found","message":"No message available","path":"/topic/minimum/length/"}
+ {"timestamp":"<TIMESTAMP>","status":404,"error":"Not Found","message":"No static resource topic/minimum/length.","path":"/topic/minimum/length/"}
```
- `request-errors/min-length-overflow`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":400,"error":"Bad Request","message":"Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'; nested exception is java.lang.NumberFormatException: For input string: \"99999999999\"","path":"/topic/minimum/length/99999999999"}
+ {"timestamp":"<TIMESTAMP>","status":400,"error":"Bad Request","message":"Method parameter 'minLength': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'; For input string: \"99999999999\"","path":"/topic/minimum/length/99999999999"}
```
- `null-id-topic/get-missing-hits-null`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":500,"error":"Internal Server Error","message":"No message available","path":"/topic/nope"}
+ {"timestamp":"<TIMESTAMP>","status":500,"error":"Internal Server Error","message":"Cannot invoke \"String.equals(Object)\" because the return value of \"hello.model.Topic.getId()\" is null","path":"/topic/nope"}
```
- `null-id-topic/min-length-hits-null`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":500,"error":"Internal Server Error","message":"No message available","path":"/topic/minimum/length/1"}
+ {"timestamp":"<TIMESTAMP>","status":500,"error":"Internal Server Error","message":"Cannot invoke \"String.length()\" because the return value of \"hello.model.Topic.getId()\" is null","path":"/topic/minimum/length/1"}
```
- `null-id-topic/string-operation-hits-null`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":500,"error":"Internal Server Error","message":"No message available","path":"/topic/string/operation"}
+ {"timestamp":"<TIMESTAMP>","status":500,"error":"Internal Server Error","message":"Cannot invoke \"java.lang.CharSequence.length()\" because \"this.text\" is null","path":"/topic/string/operation"}
```
- `null-id-topic/sort-hits-null`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":500,"error":"Internal Server Error","message":"No message available","path":"/topic/sort"}
+ {"timestamp":"<TIMESTAMP>","status":500,"error":"Internal Server Error","message":"Cannot invoke \"java.lang.Comparable.compareTo(Object)\" because the return value of \"java.util.function.Function.apply(Object)\" is null","path":"/topic/sort"}
```
- `null-id-topic/delete-hits-null`: body

```diff
- {"timestamp":"<TIMESTAMP>","status":500,"error":"Internal Server Error","message":"No message available","path":"/topic/java"}
+ {"timestamp":"<TIMESTAMP>","status":500,"error":"Internal Server Error","message":"Cannot invoke \"String.equals(Object)\" because the return value of \"hello.model.Topic.getId()\" is null","path":"/topic/java"}
```

### `options-allow-header` (1 requests)

Spring Framework 6 adds OPTIONS to the Allow header of its built-in OPTIONS handling. The status and empty body are unchanged; the old value was incomplete (GET was also allowed).

- `read-only/topics-options`: allow 'POST' -> 'POST,OPTIONS'

## All requests

| Scenario | Step | Request | Baseline | Candidate | Result |
|---|---|---|---|---|---|
| read-only | greeting-default | `GET /` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | greeting-name | `GET /?name=Devin` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | greeting-empty-name | `GET /?name=` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | greeting-post | `POST /` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | greeting-unicode | `GET /?name=%C3%9Cnic%C3%B8de` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | topics-list | `GET /topic` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | topics-list-head | `HEAD /topic` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | topics-list-trailing-slash | `GET /topic/` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | topics-list-xml-accept | `GET /topic` | 406 `no body` | 406 `no body` | identical |
| read-only | topics-options | `OPTIONS /topic` | 200 `no body` | 200 `no body` | accepted: options-allow-header |
| read-only | topic-java | `GET /topic/java` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | topic-spring | `GET /topic/spring` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | topic-java-trailing-slash | `GET /topic/java/` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | topic-missing | `GET /topic/nope` | 500 `application/json;charset=UTF-8` | 500 `application/json;charset=UTF-8` | identical |
| read-only | topic-dotted-id | `GET /topic/java.json` | 500 `application/json;charset=UTF-8` | 500 `application/json;charset=UTF-8` | identical |
| read-only | min-length-4 | `GET /topic/minimum/length/4` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | min-length-0 | `GET /topic/minimum/length/0` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | min-length-negative | `GET /topic/minimum/length/-1` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | min-length-100 | `GET /topic/minimum/length/100` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | min-length-not-a-number | `GET /topic/minimum/length/abc` | 400 `application/json;charset=UTF-8` | 400 `application/json;charset=UTF-8` | accepted: error-message-wording |
| read-only | string-operation | `GET /topic/string/operation` | 200 `text/plain;charset=UTF-8` | 200 `text/plain;charset=UTF-8` | identical |
| read-only | file-operation | `GET /topic/file/operation` | 200 `text/plain;charset=UTF-8` | 200 `text/plain;charset=UTF-8` | identical |
| read-only | datetime | `GET /datetime` | 200 `text/plain;charset=UTF-8` | 200 `text/plain;charset=UTF-8` | identical |
| read-only | static-index | `GET /index.html` | 200 `text/html` | 200 `text/html` | identical |
| read-only | unknown-path | `GET /does-not-exist` | 404 `application/json;charset=UTF-8` | 404 `application/json;charset=UTF-8` | accepted: error-message-wording |
| read-only | sort | `GET /topic/sort` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | topics-list-after-sort | `GET /topic` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| read-only | string-operation-after-sort | `GET /topic/string/operation` | 200 `text/plain;charset=UTF-8` | 200 `text/plain;charset=UTF-8` | identical |
| read-only | greeting-counter-after | `GET /` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| crud | add-python | `POST /topic` | 200 `no body` | 200 `no body` | identical |
| crud | list-after-add | `GET /topic` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| crud | get-python | `GET /topic/python` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| crud | update-python | `PUT /topic/python` | 200 `no body` | 200 `no body` | identical |
| crud | get-python-after-update | `GET /topic/python` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| crud | update-missing | `PUT /topic/missing` | 200 `no body` | 200 `no body` | identical |
| crud | update-renames-id | `PUT /topic/spring` | 200 `no body` | 200 `no body` | identical |
| crud | get-old-id-after-rename | `GET /topic/spring` | 500 `application/json;charset=UTF-8` | 500 `application/json;charset=UTF-8` | identical |
| crud | get-new-id-after-rename | `GET /topic/springboot` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| crud | add-unknown-fields | `POST /topic` | 200 `no body` | 200 `no body` | identical |
| crud | add-partial | `POST /topic` | 200 `no body` | 200 `no body` | identical |
| crud | get-partial | `GET /topic/rust` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| crud | add-duplicate-id | `POST /topic` | 200 `no body` | 200 `no body` | identical |
| crud | get-duplicate-first-wins | `GET /topic/java` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| crud | delete-java | `DELETE /topic/java` | 200 `no body` | 200 `no body` | identical |
| crud | delete-missing | `DELETE /topic/missing` | 200 `no body` | 200 `no body` | identical |
| crud | list-after-delete | `GET /topic` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| crud | min-length-after-crud | `GET /topic/minimum/length/4` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| crud | sort-after-crud | `GET /topic/sort` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| crud | string-operation-after-crud | `GET /topic/string/operation` | 200 `text/plain;charset=UTF-8` | 200 `text/plain;charset=UTF-8` | identical |
| request-errors | post-no-body | `POST /topic` | 400 `application/json;charset=UTF-8` | 400 `application/json;charset=UTF-8` | identical |
| request-errors | post-malformed-json | `POST /topic` | 400 `application/json;charset=UTF-8` | 400 `application/json;charset=UTF-8` | accepted: error-message-wording |
| request-errors | post-text-plain | `POST /topic` | 415 `application/json;charset=UTF-8` | 415 `application/json;charset=UTF-8` | accepted: error-message-wording |
| request-errors | post-json-array | `POST /topic` | 400 `application/json;charset=UTF-8` | 400 `application/json;charset=UTF-8` | accepted: error-message-wording |
| request-errors | put-no-body | `PUT /topic/java` | 400 `application/json;charset=UTF-8` | 400 `application/json;charset=UTF-8` | identical |
| request-errors | delete-collection | `DELETE /topic` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| request-errors | patch-topic | `PATCH /topic/java` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| request-errors | post-on-item | `POST /topic/java` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| request-errors | min-length-missing-segment | `GET /topic/minimum/length/` | 404 `application/json;charset=UTF-8` | 404 `application/json;charset=UTF-8` | accepted: error-message-wording |
| request-errors | min-length-overflow | `GET /topic/minimum/length/99999999999` | 400 `application/json;charset=UTF-8` | 400 `application/json;charset=UTF-8` | accepted: error-message-wording |
| request-errors | list-unchanged | `GET /topic` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| null-id-topic | add-without-id | `POST /topic` | 200 `no body` | 200 `no body` | identical |
| null-id-topic | list-with-null-id | `GET /topic` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| null-id-topic | get-existing-before-null | `GET /topic/java` | 200 `application/json;charset=UTF-8` | 200 `application/json;charset=UTF-8` | identical |
| null-id-topic | get-missing-hits-null | `GET /topic/nope` | 500 `application/json;charset=UTF-8` | 500 `application/json;charset=UTF-8` | accepted: error-message-wording |
| null-id-topic | min-length-hits-null | `GET /topic/minimum/length/1` | 500 `application/json;charset=UTF-8` | 500 `application/json;charset=UTF-8` | accepted: error-message-wording |
| null-id-topic | string-operation-hits-null | `GET /topic/string/operation` | 500 `application/json;charset=UTF-8` | 500 `application/json;charset=UTF-8` | accepted: error-message-wording |
| null-id-topic | update-hits-null | `PUT /topic/nope` | 200 `no body` | 200 `no body` | identical |
| null-id-topic | sort-hits-null | `GET /topic/sort` | 500 `application/json;charset=UTF-8` | 500 `application/json;charset=UTF-8` | accepted: error-message-wording |
| null-id-topic | delete-hits-null | `DELETE /topic/java` | 500 `application/json;charset=UTF-8` | 500 `application/json;charset=UTF-8` | accepted: error-message-wording |
