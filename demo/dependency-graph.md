# Application dependency graph

Discovered agentlessly from AWS APIs in `us-east-1` for `Project=modernization-demo`, `Track=java` (14 nodes, 19 edges). Regenerate with `make aws-discover`.

![dependency graph](dependency-graph.png)

```mermaid
graph LR
  n0{{"SG<br/>moddemo-topics-app"}}
  n1{{"SG<br/>moddemo-topics-alb"}}
  n2{{"SG<br/>moddemo-topics-db"}}
  n3("CIDR<br/>0.0.0.0/0")
  n4["EC2<br/>moddemo-topics-app"]
  n5>"Image<br/>topics-api:legacy"]
  n6(["ALB<br/>moddemo-topics-alb"])
  n7[/"TG<br/>moddemo-topics-tg"/]
  n8[("RDS<br/>moddemo-topics-db")]
  n9[["Secret<br/>moddemo-topics/db"]]
  n10(("Zone<br/>demo.internal"))
  n11("DNS<br/>app.topics.demo.internal")
  n12("DNS<br/>db.topics.demo.internal")
  n13("DNS<br/>lb.topics.demo.internal")
  n1 -->|allows_ingress port=8080| n0
  n3 -->|allows_ingress port=80| n1
  n0 -->|allows_ingress port=5432| n2
  n0 -.- n4
  n4 -->|runs_image| n5
  n1 -.- n6
  n6 -->|routes_to listener_port=80| n7
  n7 -->|routes_to port=8080| n4
  n7 -->|health_checks path=/manage/health/readiness, port=8080| n4
  n2 -.- n8
  n4 -->|reads_secret key=DB_PASSWORD| n9
  n10 -.- n11
  n11 -->|resolves_to| n4
  n10 -.- n12
  n12 -->|resolves_to| n8
  n10 -.- n13
  n13 -->|resolves_to| n6
  n4 -->|connects_to_db port=5432| n12
  n4 -->|connects_to_db port=5432| n8
```

## Edges

| from | edge | to | attrs |
|---|---|---|---|
| `sg:moddemo-topics-alb` | allows_ingress | `sg:moddemo-topics-app` | {"port": 8080, "protocol": "tcp"} |
| `cidr:0.0.0.0/0` | allows_ingress | `sg:moddemo-topics-alb` | {"port": 80, "protocol": "tcp"} |
| `sg:moddemo-topics-app` | allows_ingress | `sg:moddemo-topics-db` | {"port": 5432, "protocol": "tcp"} |
| `ec2:moddemo-topics-app` | runs_image | `image:topics-api:legacy` | {"base_image": "eclipse-temurin:11-jre"} |
| `alb:moddemo-topics-alb` | routes_to | `tg:moddemo-topics-tg` | {"listener_port": 80, "protocol": "HTTP"} |
| `tg:moddemo-topics-tg` | routes_to | `ec2:moddemo-topics-app` | {"port": 8080} |
| `tg:moddemo-topics-tg` | health_checks | `ec2:moddemo-topics-app` | {"path": "/manage/health/readiness", "port": 8080, "state": "healthy"} |
| `ec2:moddemo-topics-app` | reads_secret | `secret:moddemo-topics/db` | {"key": "DB_PASSWORD"} |
| `dns:app.topics.demo.internal` | resolves_to | `ec2:moddemo-topics-app` | {} |
| `dns:db.topics.demo.internal` | resolves_to | `rds:moddemo-topics-db` | {} |
| `dns:lb.topics.demo.internal` | resolves_to | `alb:moddemo-topics-alb` | {} |
| `ec2:moddemo-topics-app` | connects_to_db | `dns:db.topics.demo.internal` | {"host": "db.topics.demo.internal", "port": 5432} |
| `ec2:moddemo-topics-app` | connects_to_db | `rds:moddemo-topics-db` | {"via": "db.topics.demo.internal", "port": 5432} |
