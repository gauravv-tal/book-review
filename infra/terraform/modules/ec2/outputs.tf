
output "launch_template_id" {
  description = "Launch Template ID"
  value       = aws_launch_template.this.id
}

output "instance_profile_name" {
  description = "Instance profile name"
  value       = aws_iam_instance_profile.this.name
}

output "ec2_security_group_id" {
  description = "EC2 instance security group ID"
  value       = aws_security_group.ec2.id
}
