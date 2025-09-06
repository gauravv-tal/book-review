terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }
}

resource "aws_ecr_repository" "this" {
  name                 = var.repository_name
  image_tag_mutability = var.image_tag_mutability
  image_scanning_configuration {
    scan_on_push = var.scan_on_push
  }
  tags = var.tags
}

resource "aws_ecr_lifecycle_policy" "this" {
  count      = length(trim(var.lifecycle_policy_json, " ")) > 0 ? 1 : 0
  repository = aws_ecr_repository.this.name
  policy     = var.lifecycle_policy_json
}
