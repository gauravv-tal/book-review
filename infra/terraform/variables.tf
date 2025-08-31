variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "ap-south-1"
}

variable "app_name" {
  description = "Application name prefix"
  type        = string
  default     = "book-review"
}

variable "container_image" {
  description = "Container image to deploy (e.g., <account>.dkr.ecr.<region>.amazonaws.com/book-review:latest)"
  type        = string
}

variable "desired_count" {
  description = "Number of ECS tasks"
  type        = number
  default     = 1
}

variable "cpu" {
  description = "Fargate task CPU"
  type        = number
  default     = 256
}

variable "memory" {
  description = "Fargate task memory"
  type        = number
  default     = 512
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

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.micro"
}

# Optional: Secrets Manager ARNs for application runtime configuration
# If provided, ECS will inject these as container environment variables
variable "db_url_secret_arn" {
  description = "Secrets Manager ARN for DB_URL"
  type        = string
  default     = ""
}

variable "db_username_secret_arn" {
  description = "Secrets Manager ARN for DB_USERNAME"
  type        = string
  default     = ""
}

variable "db_password_secret_arn" {
  description = "Secrets Manager ARN for DB_PASSWORD"
  type        = string
  default     = ""
}
