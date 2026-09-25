variable "region" {
  type    = string
  default = "us-east-1"
}

variable "track" {
  type    = string
  default = "java"
}

variable "name" {
  description = "Resource name prefix."
  type        = string
  default     = "moddemo-topics"
}

variable "vpc_cidr" {
  type    = string
  default = "10.42.0.0/16"
}

variable "dns_zone" {
  description = "Private hosted zone attached to the VPC."
  type        = string
  default     = "demo.internal"
}

variable "instance_type" {
  type    = string
  default = "t3.small"
}

variable "db_instance_class" {
  type    = string
  default = "db.t4g.micro"
}

variable "app_image_tag" {
  description = "Tag of the image pushed to ECR by `make aws-push`."
  type        = string
  default     = "legacy"
}

# ---- Runtime contract of the LEGACY app. Everything below is what the migrated
# ---- (Java 21 / Boot 3) code no longer matches; see demo/PLANTED-GAPS.md.
variable "runtime_image" {
  description = "Base image the app container is built FROM (mirrors ../../Dockerfile)."
  type        = string
  default     = "eclipse-temurin:11-jre"
}

variable "app_port" {
  description = "Only port the app SG admits from the ALB."
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "Target-group health check path."
  type        = string
  default     = "/manage/health/readiness"
}

variable "db_secret_key" {
  description = "JSON key inside the Secrets Manager secret that the app reads as its DB password env var."
  type        = string
  default     = "DB_PASSWORD"
}
