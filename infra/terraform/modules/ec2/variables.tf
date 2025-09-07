variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "private_subnet_ids" {
  description = "List of private subnet IDs for ASG"
  type        = list(string)
}

variable "alb_security_group_id" {
  description = "ALB security group ID allowed to access instances"
  type        = string
}

variable "target_group_arn" {
  description = "Target group ARN to attach ASG instances"
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.small"
}

variable "desired_capacity" {
  description = "ASG desired capacity"
  type        = number
  default     = 1
}

variable "min_size" {
  description = "ASG min size"
  type        = number
  default     = 1
}

variable "max_size" {
  description = "ASG max size"
  type        = number
  default     = 2
}

variable "ecr_repository_url" {
  description = "ECR repository URL for the backend image"
  type        = string
}

variable "image_tag" {
  description = "Docker image tag to deploy"
  type        = string
  default     = "latest"
}

variable "container_port" {
  description = "Container port exposed by the app"
  type        = number
  default     = 8080
}

variable "region" {
  description = "AWS region"
  type        = string
}

variable "secrets_map" {
  description = "Map of secret logical names to values (used to determine if secrets policy is needed)"
  type        = map(string)
  default     = {}
}

variable "jwt_secret_arn" {
  description = "Secrets Manager ARN for JWT secret"
  type        = string
  default     = ""
}

variable "db_username_secret_arn" {
  description = "Secrets Manager ARN for DB username"
  type        = string
  default     = ""
}

variable "db_password_secret_arn" {
  description = "Secrets Manager ARN for DB password"
  type        = string
  default     = ""
}

variable "db_url" {
  description = "JDBC URL for database (can be provided via SSM or tfvar)"
  type        = string
  default     = ""
}

variable "db_url_secret_arn" {
  description = "Secrets Manager ARN for the database URL"
  type        = string
  default     = ""
}

variable "smtp_username_secret_arn" {
  description = "Secrets Manager ARN for SMTP username"
  type        = string
  default     = ""
}

variable "smtp_password_secret_arn" {
  description = "Secrets Manager ARN for SMTP password"
  type        = string
  default     = ""
}

variable "smtp_host" {
  description = "SMTP server host"
  type        = string
  default     = "smtp.gmail.com"
}

variable "smtp_port" {
  description = "SMTP server port"
  type        = string
  default     = "587"
}

variable "email_sender" {
  description = "Email sender address"
  type        = string
  default     = "no-reply@example.com"
}

variable "email_enabled" {
  description = "Enable email functionality"
  type        = bool
  default     = false
}

variable "log_group_name" {
  description = "CloudWatch Logs group name for application logs"
  type        = string
  default     = ""
}

variable "key_name" {
  description = "EC2 Key Pair name to attach to the Launch Template (optional)"
  type        = string
  default     = ""
}

variable "allow_ssh" {
  description = "Whether to allow SSH access to instances"
  type        = bool
  default     = false
}

variable "ssh_ingress_cidr" {
  description = "CIDR block allowed for SSH access (used when allow_ssh is true)"
  type        = string
  default     = ""
}

variable "tags" {
  description = "Tags to apply"
  type        = map(string)
  default     = {}
}
