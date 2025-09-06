output "service_security_group_id" {
  value       = aws_security_group.svc.id
  description = "Security group ID for the service"
}

output "rds_instance_id" {
  value       = try(aws_db_instance.this[0].id, null)
  description = "RDS instance identifier"
}

output "rds_endpoint" {
  value       = try(aws_db_instance.this[0].address, null)
  description = "RDS endpoint address"
}

output "rds_port" {
  value       = try(aws_db_instance.this[0].port, null)
  description = "RDS port"
}
