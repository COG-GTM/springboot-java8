SHELL := /bin/bash
COMPOSE := docker compose --env-file infra/.env -f infra/docker-compose.yml
MVN ?= ./mvnw   # MVN=true skips the build when target/app.jar is prebuilt (CI)
LB_URL ?= http://localhost:8088
PROM_URL ?= http://localhost:9091
GRAFANA_URL ?= http://localhost:3000
HEALTH_PATH ?= /health
LB_INTERNAL ?= http://lb.topics.internal
K6_IMAGE ?= grafana/k6:0.54.0
VUS ?= 20
DURATION ?= 30s
P95_MS ?= 250
ERR_RATE ?= 0.01
RESULTS := $(CURDIR)/load/results
BASELINE := demo/BASELINE.json

.PHONY: build image up down reset logs health smoke baseline loadtest verify compare psql report

build:
	$(MVN) -q -DskipTests package

image: build
	$(COMPOSE) build app

infra/secrets/app.env:
	cp infra/secrets/.env.example infra/secrets/app.env

up: image infra/secrets/app.env
	$(COMPOSE) up -d --remove-orphans
	@echo "waiting for load balancer $(LB_URL)$(HEALTH_PATH) ..."
	@for i in $$(seq 1 60); do \
	  if curl -fsS $(LB_URL)$(HEALTH_PATH) >/dev/null 2>&1; then echo "stack is up"; exit 0; fi; sleep 2; \
	done; echo "load balancer never became healthy"; $(COMPOSE) logs --tail=50 app nginx; exit 1

down:
	$(COMPOSE) down --remove-orphans

reset:
	$(COMPOSE) down -v --remove-orphans
	rm -rf $(RESULTS)

logs:
	$(COMPOSE) logs -f --tail=100

health:
	@echo "== nginx health check ($(LB_URL)$(HEALTH_PATH)) =="
	@curl -fsS -w '\nHTTP %{http_code}\n' $(LB_URL)$(HEALTH_PATH)

smoke: health
	@echo "== smoke: list topics =="
	@curl -fsS $(LB_URL)/topic | tee /dev/stderr | grep -q '"spring"'
	@echo "== smoke: stored function via /topic/report =="
	@curl -fsS $(LB_URL)/topic/report | tee /dev/stderr | grep -q '"bucket"'
	@echo "== smoke: reporting consumer output =="
	@$(COMPOSE) exec -T reporting sh -c 'test -s /reports/latest.json && cat /reports/latest.json' | grep -q '"report"'
	@echo "== smoke: DNS from app network =="
	@$(COMPOSE) exec -T app sh -c 'getent hosts db.topics.internal app.topics.internal lb.topics.internal'
	@echo "== smoke: prometheus target up =="
	@for i in $$(seq 1 12); do \
	  curl -fsS '$(PROM_URL)/api/v1/query?query=up%7Bjob%3D%22topics-api%22%7D' | grep -q '"value":\[[0-9.]*,"1"\]' && break; \
	  [ $$i -eq 12 ] && echo "prometheus target topics-api never came up" && exit 1; sleep 5; \
	done
	@echo "smoke OK"

# Run k6 inside the compose network so the LB is reached through the DNS name.
define run_k6
	mkdir -p $(RESULTS)
	docker run --rm --user $$(id -u):$$(id -g) --network topics_topics-net --dns 172.28.0.53 \
	  -v $(CURDIR)/load:/scripts -v $(RESULTS):/results \
	  -e BASE_URL=$(LB_INTERNAL) -e VUS=$(VUS) -e DURATION=$(DURATION) \
	  -e P95_MS=$(P95_MS) -e ERR_RATE=$(ERR_RATE) -e OUT=/results/$(1) \
	  $(K6_IMAGE) run /scripts/topics.js
endef

baseline:
	$(call run_k6,baseline.json)
	cp $(RESULTS)/baseline.json $(BASELINE)
	@echo "baseline written to $(BASELINE)"

loadtest:
	$(call run_k6,latest.json)

compare:
	@python3 infra/scripts/compare_nfr.py $(BASELINE) $(RESULTS)/latest.json

verify: smoke loadtest
	@if [ -f $(BASELINE) ]; then $(MAKE) --no-print-directory compare; else echo "no $(BASELINE); skipping NFR comparison"; fi
	@echo "verify OK"

psql:
	$(COMPOSE) exec db psql -U topics -d topics

report:
	$(COMPOSE) exec -T reporting cat /reports/latest.json

# ---------------------------------------------------------------------------
# AWS "real infra" path (infra/aws). Requires AWS credentials in the shell.
# ---------------------------------------------------------------------------
AWS_REGION ?= us-east-1
AWS_DIR := infra/aws
TF := terraform -chdir=$(AWS_DIR)
AWS_TAG ?= legacy
TRACK ?= java
PY ?= python3   # e.g. PY=.venv/bin/python after: pip install -r tools/discover/requirements.txt
AWS_BASELINE := demo/BASELINE-aws.json

.PHONY: aws-init aws-push aws-up aws-down aws-status aws-health aws-baseline aws-loadtest aws-discover aws-check-clean

aws-init:
	@aws sts get-caller-identity --region $(AWS_REGION) --query Account --output text >/dev/null || (echo "aws sts get-caller-identity failed"; exit 1)
	$(TF) init -input=false

# ECR must exist before the image can be pushed; everything else waits for the image.
aws-push: aws-init image
	$(TF) apply -input=false -auto-approve -target=aws_ecr_repository.app
	@ECR=$$($(TF) output -raw ecr_repository_url) && \
	  aws ecr get-login-password --region $(AWS_REGION) | docker login --username AWS --password-stdin $${ECR%%/*} && \
	  docker tag topics-api:$$(grep ^APP_VERSION infra/.env | cut -d= -f2) $$ECR:$(AWS_TAG) && \
	  docker push $$ECR:$(AWS_TAG)

aws-up: aws-push
	$(TF) apply -input=false -auto-approve -var app_image_tag=$(AWS_TAG)
	@ALB=$$($(TF) output -raw alb_url); echo "waiting for $$ALB/manage/health/readiness (RDS + first boot take a few minutes) ..."; \
	for i in $$(seq 1 90); do \
	  if curl -fsS $$ALB/manage/health/readiness >/dev/null 2>&1; then echo "AWS stack is up: $$ALB"; exit 0; fi; sleep 10; \
	done; echo "ALB never became healthy"; $(MAKE) --no-print-directory aws-status; exit 1

aws-status:
	@$(TF) output 2>/dev/null || true
	@aws elbv2 describe-target-health --region $(AWS_REGION) \
	  --target-group-arn $$(aws elbv2 describe-target-groups --region $(AWS_REGION) --names moddemo-topics-tg --query 'TargetGroups[0].TargetGroupArn' --output text) \
	  --query 'TargetHealthDescriptions[].TargetHealth' --output json

aws-health:
	@curl -fsS -w '\nHTTP %{http_code}\n' $$($(TF) output -raw alb_url)/manage/health/readiness

define run_k6_aws
	mkdir -p $(RESULTS)
	docker run --rm --user $$(id -u):$$(id -g) \
	  -v $(CURDIR)/load:/scripts -v $(RESULTS):/results \
	  -e BASE_URL=$$($(TF) output -raw alb_url) -e VUS=$(VUS) -e DURATION=$(DURATION) \
	  -e P95_MS=$(P95_MS) -e ERR_RATE=$(ERR_RATE) -e OUT=/results/$(1) \
	  $(K6_IMAGE) run /scripts/topics.js
endef

aws-baseline:
	$(call run_k6_aws,baseline-aws.json)
	cp $(RESULTS)/baseline-aws.json $(AWS_BASELINE)
	@echo "baseline written to $(AWS_BASELINE)"

aws-loadtest:
	$(call run_k6_aws,latest-aws.json)

# Wiz-style agentless discovery: walk the AWS APIs by tag, emit the dependency graph.
aws-discover:
	$(PY) tools/discover/discover.py --region $(AWS_REGION) --project modernization-demo --track $(TRACK) --out demo
	$(PY) tools/discover/diff.py --graph demo/dependency-graph.json --repo .

aws-down: aws-init
	$(TF) destroy -input=false -auto-approve
	$(MAKE) --no-print-directory aws-check-clean

aws-check-clean:
	infra/scripts/aws_check_clean.sh $(AWS_REGION) modernization-demo $(TRACK)
