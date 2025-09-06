# Makefile for Book Review backend build and ECR push

# ===== Config =====
REGION ?= ap-south-1
IMAGE_TAG ?= latest
BACKEND_DIR := backend
TF_ENV_DIR := infra/terraform/envs/dev

# Derive ECR repo URL from Terraform outputs (requires terraform to be initialized/applied)
ECR_REPO ?= $(shell cd $(TF_ENV_DIR) && terraform output -raw ecr_repository_url 2>/dev/null)
DOCKER_IMAGE := $(ECR_REPO):$(IMAGE_TAG)

# ===== Helpers =====
.PHONY: help
help:
	@echo "Targets:"
	@echo "  backend-build        Build Spring Boot JAR (skip tests)"
	@echo "  aws-login            Login docker to AWS ECR"
	@echo "  docker-build         Build backend Docker image"
	@echo "  docker-push          Push image to ECR (requires aws-login)"
	@echo "  image                Build + Push (backend-build, aws-login, docker-build, docker-push)"
	@echo "  tf-plan              Terraform plan with image_tag override"
	@echo "  tf-apply             Terraform apply with image_tag override"
	@echo "  outputs              Print Terraform outputs (ALB DNS, ECR URL)"
	@echo "Vars: REGION=$(REGION) IMAGE_TAG=$(IMAGE_TAG) ECR_REPO=$(ECR_REPO)"

# ===== Build Backend =====
.PHONY: backend-build
backend-build:
	mvn -q -f $(BACKEND_DIR)/pom.xml clean package -DskipTests

# ===== Docker + ECR =====
.PHONY: aws-login
aws-login:
	aws ecr get-login-password --region $(REGION) | docker login --username AWS --password-stdin $(ECR_REPO)

.PHONY: docker-build
docker-build:
	@if [ -z "$(ECR_REPO)" ]; then echo "ECR_REPO is empty. Ensure Terraform outputs are available or set ECR_REPO explicitly." && exit 1; fi
	docker build -t $(DOCKER_IMAGE) $(BACKEND_DIR)

.PHONY: docker-push
docker-push:
	@if [ -z "$(ECR_REPO)" ]; then echo "ECR_REPO is empty. Ensure Terraform outputs are available or set ECR_REPO explicitly." && exit 1; fi
	docker push $(DOCKER_IMAGE)

.PHONY: image
image: backend-build aws-login docker-build docker-push

# ===== Terraform convenience =====
.PHONY: tf-plan
tf-plan:
	cd $(TF_ENV_DIR) && terraform plan -var-file=terraform.tfvars -var image_tag=$(IMAGE_TAG) -out=apply_plan

.PHONY: tf-apply
tf-apply:
	cd $(TF_ENV_DIR) && terraform apply apply_plan || terraform apply -var-file=terraform.tfvars -var image_tag=$(IMAGE_TAG)

.PHONY: outputs
outputs:
	@cd $(TF_ENV_DIR) && \
	echo "ALB DNS: $$(terraform output -raw alb_dns_name 2>/dev/null)" && \
	echo "ECR URL: $$(terraform output -raw ecr_repository_url 2>/dev/null)"
