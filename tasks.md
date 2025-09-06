# Task Breakdown – Book Review Platform (End-to-End Pipeline First)

---

## 9. Deployment via EC2 + Secrets Manager (Terraform) and GitHub Actions

### 9.1 Plan and Cleanup
- [ ] Decide target AWS region and account for deployment (e.g., `ap-south-1`).
- [ ] Decommission/ignore prior ECS-focused infra (keep for history but do not use). Update docs to reflect EC2 path.
- [ ] Define environments: `dev` (default) and `prod` (optional) with separate state/backends.

### 9.2 Terraform Project Structure (new)
- [ ] Create `infra/terraform/` with the following modules and stacks:
  - [ ] `modules/secrets` (AWS Secrets Manager for app secrets)
  - [ ] `modules/ec2` (Launch Template, Auto Scaling Group, IAM instance profile, user-data)
  - [ ] `modules/alb` (ALB + target group + listener + HTTPS via ACM)
  - [ ] `modules/ecr` (ECR repo for backend image)
- [ ] Outputs for: ALB DNS, RDS endpoint, Secrets ARNs, EC2 ASG name, ECR URL.

### 9.3 Networking and Security
- [ ] VPC with 2+ AZs, public subnets (ALB/bastion), private subnets (EC2, RDS).
- [ ] Security groups:
  - [ ] ALB SG: allow 80/443 from internet; forward to EC2 target group port (e.g., 8080).
  - [ ] EC2 SG: allow from ALB SG; egress to RDS, ECR, Secrets Manager, SSM.
  - [ ] RDS SG: allow 5432 from EC2 SG only.
- [ ] Optional: SSM Session Manager enabled on instances; disable public SSH if not needed.

### 9.4 Secrets and Configuration
- [ ] Create Secrets Manager secrets:
  - [ ] `/book-review/jwt/secret`
  - [ ] `/book-review/db/username`
  - [ ] `/book-review/db/password`
  - [ ] `/book-review/ses/sender` (optional)
  - [ ] Any external API keys (e.g., `PERPLEXITY_API_KEY`) as separate secrets.
- [ ] Allow EC2 instance role `secretsmanager:GetSecretValue` for these ARNs.
- [ ] Non-sensitive config via SSM Parameter Store (e.g., `SPRING_PROFILES_ACTIVE=prod`).
- [ ] Plan Spring Boot config to read from env vars; map secrets to env at runtime.

### 9.5 Compute (EC2) and Runtime
- [ ] ECR repo for backend; tag strategy: `main-<sha>` and `latest`.
- [ ] Launch Template with:
  - [ ] IAM instance profile (ECR pull, Secrets read, CloudWatch logs, SSM)
  - [ ] User data script to:
    - [ ] Install Docker + awscli + amazon-ssm-agent (if not in AMI)
    - [ ] `aws ecr get-login-password | docker login`
    - [ ] Pull image tag (e.g., `latest` or from SSM parameter)
    - [ ] Retrieve secrets with AWS CLI and export as env vars
    - [ ] Run Spring Boot container with proper ports, restart policy, and CloudWatch Logs integration (via awslogs driver or CW agent)
- [ ] Auto Scaling Group across 2+ AZs; min=1 (dev), desired=1, max=2.
- [ ] ALB target group health checks against `/actuator/health`.
- [ ] HTTPS via ACM cert on ALB; redirect HTTP->HTTPS.

### 9.6 Database (RDS PostgreSQL)
- [ ] Provision RDS (Postgres), instance class (dev-friendly), storage, backups, deletion protection (toggle per env).
- [ ] Store DB creds in Secrets Manager; pass DB host/port/name via env/SSM.
- [ ] Security hardening: public access off, encrypted at rest, SG restrictions.
- [ ] Flyway/Liquibase (if used) strategy defined; otherwise rely on JPA schema for dev only.

### 9.7 Frontend Deployment
- [ ] Decision: keep S3 + CloudFront for React static hosting (recommended) or Nginx on EC2.
  - [ ] If S3+CF: create bucket, OAC, and distribution; CI to upload artifacts and invalidate cache.
  - [ ] If EC2: add second container (Nginx) or host with the same instance; adjust user data and ALB rules.

### 9.8 Observability and Ops
- [ ] CloudWatch Logs for backend app logs.
- [ ] CloudWatch Alarms: high 5xx on ALB, high latency, low healthy hosts, instance CPU/memory.
- [ ] Metrics dashboard (ALB, ASG, RDS).
- [ ] Error budget and rollback plan (repoint ASG to previous image tag; Instance Refresh).

### 9.9 GitHub Actions CI/CD
- [ ] Configure OIDC or long-lived AWS credentials in GitHub (prefer OIDC role with trust policy).
- [ ] Backend workflow:
  - [ ] Trigger on push to `main`
  - [ ] Build and test Maven project
  - [ ] Build Docker image, tag as `main-<sha>` and `latest`
  - [ ] Push to ECR
  - [ ] Option A (immutable): Update Launch Template with new image tag via Terraform, then `terraform plan` + `apply`, trigger ASG Instance Refresh
  - [ ] Option B (mutable): Use SSM RunCommand to `docker pull` and restart service on instances (no template change)
- [ ] Infra workflow:
  - [ ] `terraform fmt`/`validate`/`init`/`plan` on PRs
  - [ ] `apply` to `dev` on merge to `main` with manual approval
  - [ ] Optional `prod` with `environment` protection rules
- [ ] Frontend workflow:
  - [ ] Build React app
  - [ ] If S3+CF: sync to S3 and invalidate CloudFront
  - [ ] If EC2: copy artifacts and restart Nginx container/service

### 9.10 Application Configuration Mapping
- [ ] Map Secrets Manager values to env for Spring Boot:
  - [ ] `JWT_SECRET` -> used by `JwtService`
  - [ ] `DB_USERNAME`, `DB_PASSWORD`, `DB_HOST`, `DB_PORT`, `DB_NAME`
  - [ ] `EMAIL_ENABLED`, `SES_SENDER`
  - [ ] Any third-party API keys
- [ ] Update `application-prod.properties` to use env placeholders (already present patterns) and verify profile activation via `SPRING_PROFILES_ACTIVE=prod`.

### 9.11 Runbook and Documentation
- [ ] Update README with deployment steps for EC2 path
- [ ] Document how to rotate secrets (manual and automated)
- [ ] Document rollback procedure (ASG Instance Refresh to previous LT version / SSM pull previous tag)
- [ ] Document how to access logs and metrics; health-check endpoints

### 9.12 Decommission ECS (Optional)
- [ ] Archive or remove previous ECS Terraform stacks and GH workflows
- [ ] Remove unused ECR/ECS resources if any; cleanup IAM roles/policies
- [ ] Update architecture docs to reflect EC2 path

## 1. Initial End-to-End Pipeline (Hello World)

### 1.1 Backend (Spring Boot)
- [ ] Initialize Spring Boot project (basic structure, Gradle/Maven setup)
- [ ] Implement a simple Hello World REST API endpoint (e.g., `/api/hello`)
- [ ] Dockerize the Spring Boot application (Dockerfile)
- [ ] Write unit test for Hello World endpoint

### 1.2 Frontend (React)
- [ ] Initialize React project (basic structure)
- [ ] Create a simple Hello World page
- [ ] Integrate React frontend with backend API (fetch `/api/hello` and display response)

### 1.3 Infrastructure (Terraform + AWS)
- [ ] Write Terraform scripts to provision:
  - [ ] AWS ECS cluster for backend (with Docker image deployment)
  - [ ] AWS RDS (PostgreSQL, placeholder for future use)
  - [ ] AWS S3 bucket for frontend static hosting
  - [ ] AWS CloudFront distribution for frontend
  - [ ] IAM roles and policies for ECS, S3, etc.
- [ ] Configure networking (VPC, subnets, security groups)

### 1.4 CI/CD Pipeline (GitHub Actions)
- [ ] Set up GitHub Actions workflow for backend:
  - [ ] Build and test Spring Boot app
  - [ ] Build and push Docker image to Amazon ECR
  - [ ] Deploy to ECS via Terraform
- [ ] Set up GitHub Actions workflow for frontend:
  - [ ] Build React app
  - [ ] Deploy static files to S3
  - [ ] Invalidate CloudFront cache

### 1.5 End-to-End Validation
- [ ] Access deployed frontend via CloudFront URL
- [ ] Verify frontend fetches Hello World message from backend API deployed on ECS

---

## 2. Documentation
- [ ] Update README with setup, build, and deployment instructions
- [ ] Document environment variables and secrets management

---

## 3. Next Steps (Post Hello World)
- [ ] Expand backend and frontend features per PRD and design
- [ ] Add user authentication, book management, reviews, recommendations, etc.

---

## 4. Feature Implementation (from PRD & Design)

### 4.1 Iterative Milestones (Combined Backend + Frontend)

— Milestone 1: Auth + Signup Email + Swagger
- [ ] Backend: Spring Security (JWT), entities `User` and roles (USER, MODERATOR, ADMIN)
- [ ] Backend: `/auth/signup`, `/auth/login`, `/auth/logout` with BCrypt hashing and validation
- [ ] Backend: Send signup confirmation email via AWS SES (toggleable)
  - [ ] Verify SES identity (domain/sender) and add minimal SES client/service
  - [ ] Config via env/Secrets Manager: `SES_REGION`, `SES_SENDER`, feature flag `EMAIL_ENABLED`
- [ ] Frontend: Pages for Login, Signup, Logout; store JWT (memory + refresh on reload via localStorage) and attach to API calls
- [ ] Frontend: Basic header/nav with auth state; simple protected route example
- [ ] API Docs: Swagger UI wired with Bearer auth; update endpoint summaries
- [ ] Tests: Unit tests for auth services and controller; basic UI test for auth flow
- [ ] Frontend: Impement post action routes & pages (Signup, Login, Logout)
- [ ] Fronend: On success Signup page will route to Login page
- [ ] Fronend: On success Login page will route to Home page (Welcome message for Book review platform)
- [ ] Frontend: On success Logout page will route to Login page
- [ ] Frontend: Swagger UI wired with Bearer auth; update endpoint summaries

— Milestone 2: Books Catalog (Import + List/Details)
- [ ] Backend: `Book` entity (title, author, description, cover_url, genres, year)
- [ ] Backend: Admin-only import endpoint (CSV upload) and CSV parser
- [ ] Backend: List/search (pagination; filter by title, author, genre, year)
- [ ] Frontend: Books list with search + pagination; Book details page (metadata, cover)
- [ ] Data: Define CSV format and add `books_import.csv` sample
- [ ] Tests: Service tests for import and search; component tests for list/search UI

— Milestone 3: Reviews & Ratings
- [ ] Backend: `Review` entity (book_id, user_id, text, rating, timestamps); one review per user/book
- [ ] Backend: CRUD for own review; aggregate avg rating (1 decimal) + total count on book
- [ ] Backend: Update aggregates on create/update/delete (service layer)
- [ ] Frontend: Star rating UI; create/edit/delete own review on book detail page
- [ ] Tests: Aggregation logic tests; controller tests; UI tests for review actions

— Milestone 4: Favourites & Profile
- [ ] Backend: `Favourite` entity (user_id, book_id, created_at)
- [ ] Backend: Endpoints to mark/unmark favourite; list user favourites
- [ ] Frontend: Favourite toggle on list/detail; Profile pages for My Reviews and My Favourites
- [ ] Tests: Repo/service tests and basic UI tests

— Milestone 5: Recommendations
- [ ] Backend: `/recommendations` endpoint
- [ ] Top-rated books
- [ ] Perplexity Pro API integration (env: `PERPLEXITY_API_KEY`) 
- [ ] Frontend: Recommendations page/section
- [ ] Tests: Service stub tests and UI smoke tests

— Milestone 6: Admin/Moderator Tools
- [ ] Backend: Admin endpoints for managing books/users/reviews; Moderator actions (hide/remove content)
- [ ] Frontend: Role-aware UI (show admin/mod tools based on roles)
- [ ] Security: Method-level security annotations for admin/mod actions
- [ ] Tests: RBAC tests for endpoints and UI visibility

— Milestone 7: Observability, Docs, Quality
- [ ] API Documentation: Ensure Swagger/OpenAPI for all endpoints
- [ ] Logging: Structured logs + request/response snippets for key endpoints
- [ ] Health checks: `/actuator/health` liveness/readiness
- [ ] Testing & Coverage: Unit + integration tests targeting >=80% backend coverage

— Milestone 8: Infra & CI/CD tie-ins (as features demand)
- [ ] Infra: Task env/Secrets from SSM/Secrets Manager (DB creds, JWT secret, SES sender, LLM key)
- [ ] Infra: ALB for backend, least-privilege security groups
- [ ] Frontend hosting: S3 + CloudFront; OAC, cache invalidation
- [ ] CI/CD: Build, test, coverage report; Docker to ECR; Terraform plan/apply with approvals

