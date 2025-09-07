terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }

  # Local backend for dev; replace with S3 + DynamoDB for prod/multi-user
  backend "local" {
    path = "terraform.tfstate"
  }
}

provider "aws" {
  region = var.region
}

# Data sources for default VPC and subnets (adjust for custom VPC if needed)
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "public" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }

  filter {
    name   = "map-public-ip-on-launch"
    values = ["true"]
  }
}

data "aws_subnets" "private" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }

  filter {
    name   = "map-public-ip-on-launch"
    values = ["false"]
  }
}

# ECR module
module "ecr" {
  source            = "../../modules/ecr"
  repository_name   = var.repository_name
  image_tag_mutability = "MUTABLE"
  scan_on_push      = true
  tags              = var.tags
}

# Secrets module
module "secrets" {
  source       = "../../modules/secrets"
  name_prefix  = var.app_name
  secrets_map  = var.secrets_map
  tags         = var.tags
}

# ALB module
module "alb" {
  source            = "../../modules/alb"
  name_prefix       = var.app_name
  vpc_id            = data.aws_vpc.default.id
  public_subnet_ids = data.aws_subnets.public.ids
  certificate_arn   = var.certificate_arn
  target_port       = var.target_port
  health_check_path = var.health_check_path
  tags              = var.tags
}

# Frontend (S3 + CloudFront)
module "frontend" {
  source       = "../../modules/frontend"
  name_prefix  = var.app_name
  index_document = "index.html"
  error_document = "index.html"
  price_class    = "PriceClass_100"
  tags         = var.tags
}

# CloudWatch Logs group for backend application logs
resource "aws_cloudwatch_log_group" "backend" {
  name              = "/${var.app_name}/backend"
  retention_in_days = 14
  tags              = var.tags
}

# EC2 module
module "ec2" {
  source                    = "../../modules/ec2"
  name_prefix               = var.app_name
  vpc_id                    = data.aws_vpc.default.id
  private_subnet_ids        = data.aws_subnets.public.ids
  alb_security_group_id     = module.alb.alb_security_group_id
  target_group_arn          = module.alb.target_group_arn
  instance_type             = var.instance_type
  desired_capacity          = var.desired_capacity
  min_size                  = var.min_size
  max_size                  = var.max_size
  ecr_repository_url        = module.ecr.repository_url
  image_tag                 = var.image_tag
  container_port            = var.target_port
  region                    = var.region
  secrets_map               = var.secrets_map
  jwt_secret_arn            = lookup(module.secrets.secret_arns, "jwt/secret", "")
  db_username_secret_arn    = lookup(module.secrets.secret_arns, "db/username", "")
  db_password_secret_arn    = lookup(module.secrets.secret_arns, "db/password", "")
  smtp_username_secret_arn  = lookup(module.secrets.secret_arns, "smtp/username", "")
  smtp_password_secret_arn  = lookup(module.secrets.secret_arns, "smtp/password", "")
  db_url                    = var.db_url
  smtp_host                 = var.smtp_host
  smtp_port                 = var.smtp_port
  email_sender              = var.email_sender
  email_enabled             = var.email_enabled
  log_group_name            = aws_cloudwatch_log_group.backend.name
  tags                      = var.tags
}
