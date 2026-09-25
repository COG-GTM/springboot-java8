# Pins the runtime for topics-api. `terraform plan` is enough for the demo; apply builds the same
# image docker-compose does (docker provider).

resource "docker_image" "runtime" {
  name         = var.runtime_image
  keep_locally = true
}

resource "docker_image" "app" {
  name = "topics-api:${var.app_version}"
  build {
    context    = "${path.module}/../.."
    dockerfile = "Dockerfile"
    build_args = {
      BASE_IMAGE = docker_image.runtime.name
    }
    label = {
      "com.datadoghq.tags.service" = "topics-api"
      "com.datadoghq.tags.version" = var.app_version
      "runtime.java.major"         = tostring(var.runtime_java_major)
    }
  }
  triggers = {
    jar = filesha256("${path.module}/../../target/app.jar")
  }
}

output "runtime_image" {
  value = var.runtime_image
}

output "health_check_url" {
  value = "http://app.topics.internal:${var.app_port}${var.actuator_base_path}/health/readiness"
}

output "metrics_url" {
  value = "http://app.topics.internal:${var.app_port}${var.actuator_base_path}/prometheus"
}
