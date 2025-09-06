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

variable "tags" {
  description = "Common tags"
  type        = map(string)
  default     = {
    Environment = "dev"
    Project     = "book-review"
  }
}
