terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }
}

locals {
  secrets = var.secrets_map
}

# Create a secret per entry in secrets_map
resource "aws_secretsmanager_secret" "this" {
  for_each                = local.secrets
  name                    = "${var.name_prefix}/${each.key}"
  recovery_window_in_days = 0
  tags                    = var.tags
}

# Optional: create initial version if a non-empty value is provided
resource "aws_secretsmanager_secret_version" "initial" {
  for_each      = { for k, v in local.secrets : k => v if length(trim(v, " ")) > 0 }
  secret_id     = aws_secretsmanager_secret.this[each.key].id
  secret_string = each.value
}
