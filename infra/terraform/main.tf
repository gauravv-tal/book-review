data "aws_caller_identity" "current" {}

data "aws_region" "current" {}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# Security group for the application/service (allow 8080 from anywhere)
resource "aws_security_group" "svc" {
  name        = "${var.app_name}-svc-sg1"
  description = "Allow HTTP 8080"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Optional: RDS PostgreSQL (disabled by default)
resource "aws_db_subnet_group" "this" {
  count      = var.create_rds ? 1 : 0
  name       = "${var.app_name}-db-subnet1"
  subnet_ids = data.aws_subnets.default.ids
}

resource "aws_security_group" "db" {
  count       = var.create_rds ? 1 : 0
  name        = "${var.app_name}-db-sg1"
  description = "DB access from service"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.svc.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_instance" "this" {
  count                  = var.create_rds ? 1 : 0
  identifier             = "${var.app_name}-postgres"
  engine                 = "postgres"
  engine_version         = "16.3"
  instance_class         = var.db_instance_class
  username               = var.db_username
  password               = var.db_password
  allocated_storage      = 20
  db_subnet_group_name   = aws_db_subnet_group.this[0].name
  vpc_security_group_ids = [aws_security_group.db[0].id]
  publicly_accessible    = false
  skip_final_snapshot    = true
}
