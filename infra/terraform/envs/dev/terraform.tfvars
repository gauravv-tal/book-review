# Dev environment configuration
region = "ap-south-1"

app_name = "book-review"

repository_name = "book-review1"

# Secrets to create in Secrets Manager (empty strings create empty secrets)
secrets_map = {
  "jwt/secret"    = ""  # Populate via AWS console or CLI after creation
  "db/username"   = ""
  "db/password"   = ""
}

# ACM certificate ARN (replace with your actual ARN from ACM)
certificate_arn = "arn:aws:acm:ap-south-1:YOUR_ACCOUNT_ID:certificate/YOUR_CERT_ID"

# Container and target port
target_port = 8080
health_check_path = "/actuator/health"

# EC2 configuration
instance_type = "t3.small"
desired_capacity = 1
min_size = 1
max_size = 2
image_tag = "latest"

# Database URL (placeholder for dev; replace with RDS endpoint when ready)
db_url = "jdbc:h2:mem:testdb"

# Common tags
tags = {
  Environment = "dev"
  Project     = "book-review"
}
