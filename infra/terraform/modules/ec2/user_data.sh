#!/bin/bash
set -euxo pipefail

REGION="${region}"
ECR_REPO_URL="${ecr_repo_url}"
IMAGE_TAG="${image_tag}"
CONTAINER_PORT="${container_port}"
JWT_SECRET_ARN="${jwt_secret_arn}"
DB_USERNAME_SECRET_ARN="${db_username_secret_arn}"
DB_PASSWORD_SECRET_ARN="${db_password_secret_arn}"
SMTP_USERNAME_SECRET_ARN="${smtp_username_secret_arn}"
SMTP_PASSWORD_SECRET_ARN="${smtp_password_secret_arn}"
DB_URL="${db_url}"
SMTP_HOST="${smtp_host}"
SMTP_PORT="${smtp_port}"
EMAIL_SENDER="${email_sender}"
EMAIL_ENABLED="${email_enabled}"
LOG_GROUP_NAME="${log_group_name}"

# Install updates and dependencies
amazon-linux-extras install docker -y || yum install -y docker
systemctl enable docker
systemctl start docker

# Login to ECR
aws --region "$REGION" ecr get-login-password | docker login --username AWS --password-stdin "$ECR_REPO_URL"

# Fetch secrets
get_secret() {
  local arn="$1"
  if [ -n "$arn" ]; then
    aws --region "$REGION" secretsmanager get-secret-value --secret-id "$arn" --query SecretString --output text
  fi
}

JWT_SECRET=$(get_secret "$JWT_SECRET_ARN" || echo "")
DB_USERNAME=$(get_secret "$DB_USERNAME_SECRET_ARN" || echo "")
DB_PASSWORD=$(get_secret "$DB_PASSWORD_SECRET_ARN" || echo "")
SMTP_USERNAME=$(get_secret "$SMTP_USERNAME_SECRET_ARN" || echo "")
SMTP_PASSWORD=$(get_secret "$SMTP_PASSWORD_SECRET_ARN" || echo "")

# Pull image
IMAGE="$ECR_REPO_URL:$IMAGE_TAG"
docker pull "$IMAGE"

# Stop previous container if exists
if docker ps -a --format '{{.Names}}' | grep -q '^book-review$'; then
  docker rm -f book-review || true
fi

# Run container
# Map standard Spring Boot envs; adjust as per application-prod.properties expectations

docker run -d --restart=always \
  --name book-review \
  --log-driver awslogs \
  --log-opt awslogs-region="$REGION" \
  --log-opt awslogs-group="$LOG_GROUP_NAME" \
  --log-opt awslogs-create-group=true \
  --log-opt awslogs-stream="book-review-$(hostname)" \
  -p ${container_port}:${container_port} \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SERVER_PORT=${container_port} \
  -e JWT_SECRET="$JWT_SECRET" \
  -e DB_USERNAME="$DB_USERNAME" \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -e DB_URL="$DB_URL" \
  -e SMTP_HOST="$SMTP_HOST" \
  -e SMTP_PORT="$SMTP_PORT" \
  -e SMTP_USERNAME="$SMTP_USERNAME" \
  -e SMTP_PASSWORD="$SMTP_PASSWORD" \
  -e EMAIL_SENDER="$EMAIL_SENDER" \
  -e EMAIL_ENABLED="$EMAIL_ENABLED" \
  "$IMAGE"

# Optional: install CloudWatch Agent via SSM if needed (not configured here)
