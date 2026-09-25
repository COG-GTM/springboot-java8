variable "runtime_image" {
  description = "JRE base image for topics-api. Must match infra/.env RUNTIME_IMAGE and the Dockerfile default."
  type        = string
  default     = "eclipse-temurin:11-jre"
}

variable "runtime_java_major" {
  description = "Java major version the runtime image provides; class files above this fail with UnsupportedClassVersionError."
  type        = number
  default     = 11
}

variable "app_version" {
  type    = string
  default = "0.1.0"
}

variable "app_port" {
  description = "Port the LB, firewall and Prometheus expect the app (and its actuator) on."
  type        = number
  default     = 8080
}

variable "actuator_base_path" {
  type    = string
  default = "/manage"
}
