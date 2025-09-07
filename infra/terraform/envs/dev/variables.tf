variable "region" {
  description = "AWS region"
  type        = string
  default     = "ap-south-1"
}

variable "app_name" {
  description = "Application name prefix"
  type        = string
  default     = "book-review"
}

variable "repository_name" {
  description = "ECR repository name"
  type        = string
  default     = "book-review"
}

variable "secrets_map" {
  description = "Map of secret logical names to initial values"
  type        = map(string)
  default     = {}
}

variable "certificate_arn" {
  description = "ACM certificate ARN for HTTPS"
  type        = string
}

variable "target_port" {
  description = "Container port"
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "Health check path"
  type        = string
  default     = "/actuator/health"
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

variable "image_tag" {
  description = "Docker image tag"
  type        = string
  default     = "latest"
}

variable "db_url" {
  description = "Database JDBC URL"
  type        = string
  default     = "jdbc:h2:mem:testdb"
}

variable "db_url_secret_arn" {
  description = "Secrets Manager ARN for the database URL"
  type        = string
  default     = ""
}

variable "create_rds" {
  description = "Whether to create RDS PostgreSQL"
  type        = bool
  default     = false
}

variable "db_username" {
  description = "RDS master username"
  type        = string
  default     = "appuser"
}

variable "db_password" {
  description = "RDS master password"
  type        = string
  sensitive   = true
  default     = "ChangeMeStrong!123"
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

variable "smtp_username" {
  description = "SMTP username"
  type        = string
  default     = ""
}

variable "smtp_password" {
  description = "SMTP password"
  type        = string
  sensitive   = true
  default     = ""
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

variable "tags" {
  description = "Common tags"
  type        = map(string)
  default     = {
    Environment = "dev"
    Project     = "book-review"
  }
}
