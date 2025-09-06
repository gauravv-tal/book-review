output "secret_arns" {
  description = "Map of secret logical names to their ARNs"
  value       = { for k, s in aws_secretsmanager_secret.this : k => s.arn }
}
