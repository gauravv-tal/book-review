output "alb_dns_name" {
  description = "ALB DNS name"
  value       = module.alb.alb_dns_name
}

output "secret_arns" {
  description = "Map of secret logical names to their ARNs"
  value       = module.secrets.secret_arns
}

output "ecr_repository_url" {
  description = "ECR repository URL"
  value       = module.ecr.repository_url
}

