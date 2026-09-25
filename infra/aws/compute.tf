data "aws_caller_identity" "current" {}

data "aws_ssm_parameter" "al2023" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

locals {
  ecr_registry = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com"
  app_image    = "${aws_ecr_repository.app.repository_url}:${var.app_image_tag}"
  db_host      = "db.topics.${var.dns_zone}"
}

# ---- IAM: pull from ECR, read ONE secret, SSM for shell access (no SSH keys).
data "aws_iam_policy_document" "assume_ec2" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "app" {
  name               = "${var.name}-app"
  assume_role_policy = data.aws_iam_policy_document.assume_ec2.json
}

data "aws_iam_policy_document" "app" {
  statement {
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }
  statement {
    actions   = ["ecr:BatchGetImage", "ecr:GetDownloadUrlForLayer", "ecr:BatchCheckLayerAvailability"]
    resources = [aws_ecr_repository.app.arn]
  }
  statement {
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [aws_secretsmanager_secret.db.arn]
  }
}

resource "aws_iam_role_policy" "app" {
  role   = aws_iam_role.app.id
  policy = data.aws_iam_policy_document.app.json
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.app.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "app" {
  name = "${var.name}-app"
  role = aws_iam_role.app.name
}

# ---- App host (legacy container, pulled from ECR)
resource "aws_instance" "app" {
  ami                         = data.aws_ssm_parameter.al2023.value
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public[0].id
  vpc_security_group_ids      = [aws_security_group.app.id]
  iam_instance_profile        = aws_iam_instance_profile.app.name
  associate_public_ip_address = true
  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/user-data.sh.tftpl", {
    region        = var.region
    ecr_registry  = local.ecr_registry
    app_image     = local.app_image
    app_port      = var.app_port
    secret_arn    = aws_secretsmanager_secret.db.arn
    db_secret_key = var.db_secret_key
    db_host       = local.db_host
    schema_sql    = file("${path.module}/../db/init/01-schema.sql")
    seed_sql      = file("${path.module}/../db/init/02-seed.sql")
    reporting_sql = file("${path.module}/../db/init/03-reporting.sql")
  })

  metadata_options {
    http_tokens = "required"
  }

  tags = {
    Name         = "${var.name}-app"
    RuntimeImage = var.runtime_image
    AppImage     = local.app_image
  }

  depends_on = [aws_db_instance.topics, aws_route53_record.db]
}

# ---- Load balancer
resource "aws_lb" "app" {
  name               = "${var.name}-alb"
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id
}

resource "aws_lb_target_group" "app" {
  name     = "${var.name}-tg"
  port     = var.app_port
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    path                = var.health_check_path
    port                = "traffic-port"
    matcher             = "200"
    interval            = 10
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
  deregistration_delay = 10
}

resource "aws_lb_target_group_attachment" "app" {
  target_group_arn = aws_lb_target_group.app.arn
  target_id        = aws_instance.app.id
  port             = var.app_port
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.app.arn
  port              = 80
  protocol          = "HTTP"
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}

# ---- DNS: private zone, same names as infra/dns/hosts (suffixed with the zone)
resource "aws_route53_zone" "private" {
  name = var.dns_zone
  vpc {
    vpc_id = aws_vpc.main.id
  }
}

resource "aws_route53_record" "lb" {
  zone_id = aws_route53_zone.private.zone_id
  name    = "lb.topics.${var.dns_zone}"
  type    = "A"
  alias {
    name                   = aws_lb.app.dns_name
    zone_id                = aws_lb.app.zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "app" {
  zone_id = aws_route53_zone.private.zone_id
  name    = "app.topics.${var.dns_zone}"
  type    = "A"
  ttl     = 60
  records = [aws_instance.app.private_ip]
}

resource "aws_route53_record" "db" {
  zone_id = aws_route53_zone.private.zone_id
  name    = local.db_host
  type    = "CNAME"
  ttl     = 60
  records = [aws_db_instance.topics.address]
}
