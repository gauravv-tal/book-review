variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "public_subnet_ids" {
  description = "List of public subnet IDs for ALB"
  type        = list(string)
}

variable "certificate_arn" {
  description = "ACM certificate ARN for HTTPS listener"
  type        = string
}

variable "target_port" {
  description = "Port that targets (EC2 instances) listen on"
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "Health check path for target group"
  type        = string
  default     = "/actuator/health"
}

variable "tags" {
  description = "Tags to apply"
  type        = map(string)
  default     = {}
}
