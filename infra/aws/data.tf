resource "random_password" "db" {
  length  = 24
  special = false
}

# Legacy secret contract: one JSON key, named after the env var the Boot 2.7 app reads.
resource "aws_secretsmanager_secret" "db" {
  name                    = "${var.name}/db"
  description             = "topics-api database credentials (legacy key name)"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id = aws_secretsmanager_secret.db.id
  secret_string = jsonencode({
    (var.db_secret_key) = random_password.db.result
    DB_USER             = "topics"
  })
}

resource "aws_db_subnet_group" "db" {
  name       = "${var.name}-db"
  subnet_ids = aws_subnet.public[*].id
}

resource "aws_db_instance" "topics" {
  identifier              = "${var.name}-db"
  engine                  = "postgres"
  engine_version          = "16"
  instance_class          = var.db_instance_class
  allocated_storage       = 20
  storage_type            = "gp3"
  db_name                 = "topics"
  username                = "topics"
  password                = random_password.db.result
  db_subnet_group_name    = aws_db_subnet_group.db.name
  vpc_security_group_ids  = [aws_security_group.db.id]
  publicly_accessible     = false
  skip_final_snapshot     = true
  deletion_protection     = false
  backup_retention_period = 0
  apply_immediately       = true
  tags                    = { Name = "${var.name}-db" }
}

resource "aws_ecr_repository" "app" {
  name                 = "${var.name}/topics-api"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}
