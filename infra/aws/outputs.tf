output "alb_dns_name" {
  value = aws_lb.app.dns_name
}

output "alb_url" {
  value = "http://${aws_lb.app.dns_name}"
}

output "health_check_url" {
  value = "http://${aws_lb.app.dns_name}${var.health_check_path}"
}

output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}

output "app_image" {
  value = local.app_image
}

output "app_instance_id" {
  value = aws_instance.app.id
}

output "db_endpoint" {
  value = aws_db_instance.topics.address
}

output "db_dns_name" {
  value = local.db_host
}

output "secret_arn" {
  value = aws_secretsmanager_secret.db.arn
}

output "runtime_image" {
  value = var.runtime_image
}
