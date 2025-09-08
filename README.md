# Book Review – Local Setup Guide

This guide walks you through running the project locally end‑to‑end: backend (Spring Boot), frontend (React), infrastructure (Terraform), and generating JaCoCo coverage reports.

## Prerequisites
- Java 17 (Temurin recommended)
- Maven 3.8+
- Node.js 18+ and npm 9+
- Docker (optional for local Postgres)
- Terraform 1.6+
- AWS CLI v2 (for Terraform and ECR/CloudFront tasks)
- jq (optional, used in scripts/CI)

---
## Backend (Spring Boot)
Location: `backend/`

### 1) Configure local environment
By default, the app uses the `dev` profile (see `backend/src/main/resources/application.properties`). For local runs, you can supply settings via environment variables or an `application-dev.properties` (gitignored).

Minimum settings for local dev:
- Database URL
- Username/password
- JWT secret

Example: run a local Postgres via Docker then export environment variables.

```bash
# Start local Postgres
docker run --name br-pg -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:15

# Create DB and user (optional; defaults shown)
# You can use psql, PGAdmin, or a GUI to create a DB e.g., 'bookreview'

# Set environment variables for Spring Boot
export DB_URL="jdbc:postgresql://localhost:5432/postgres"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
export JWT_SECRET="dev-secret-key-change-me"
```

Alternatively create `backend/src/main/resources/application-dev.properties` with your local settings (kept out of Git).

### 2) Run the backend
```bash
cd backend
mvn spring-boot:run
# App listens on http://localhost:8080
```

Useful endpoints:
- Health: `GET http://localhost:8080/actuator/health`
- Books: `GET http://localhost:8080/books`
- Auth: `POST http://localhost:8080/auth/login` | `POST /auth/signup` | `POST /auth/logout`

CORS for local:
- The backend allows common localhost and wildcard patterns (configurable via `app.cors.allowed-origin-patterns`).

---
## Frontend (React)
Location: `frontend/`

### 1) Configure API base URL
Create `frontend/.env.local` with:
```
REACT_APP_API_BASE=http://localhost:8080
```
This repo is already wired to read `REACT_APP_API_BASE` (see `frontend/src/AuthContext.js`).

### 2) Install and run
```bash
cd frontend
npm install
npm start
# Opens http://localhost:3000
```

For production build:
```bash
npm run build
```
See `frontend/DEPLOYMENT.md` for production deployment notes and using repo/CI secrets.

---
## Terraform (Infrastructure)
Location: `infra/terraform/`

This sets up S3 (static hosting), CloudFront, ALB, EC2/ASG, etc. For local development you do not need Terraform. Use it only when provisioning/updating cloud infra.

Important security practices (do not commit secrets):
- Do not commit real `terraform.tfvars` or `*.tfstate` files.
- Use `terraform.tfvars.example` with placeholders.
- Store real values in CI/CD secrets or local `terraform.tfvars` kept out of Git.

### Working with the dev environment
```bash
cd infra/terraform/envs/dev

# Initialize providers/modules
terraform init

# Copy example and fill your values (kept local)
cp ../../terraform.tfvars.example ./terraform.tfvars
# Edit terraform.tfvars with your AWS account IDs, domain names, etc.

# Plan/apply
terraform plan -out=bk_review_plan
terraform apply bk_review_plan
```

CloudFront changes may take 10–15 minutes to propagate. Consider invalidating the distribution after content/origin changes.

---
## JaCoCo Coverage Report
Location after test: `backend/target/site/jacoco/index.html`

Generate coverage:
```bash
cd backend
mvn -q clean test
# or
mvn -q verify
```
Open the HTML report in your browser:
```bash
open target/site/jacoco/index.html  # macOS
```

---
## CI/CD Overview
GitHub Actions workflows:
- Backend build & push to ECR: `.github/workflows/backend-ecr.yml`
  - Tags both the immutable SHA tag and `latest`.
- Frontend deploy to S3/CloudFront: `.github/workflows/frontend-s3-cf.yml`
  - Uses `REACT_APP_API_BASE` from repo/environment secrets for production builds.

---
## Troubleshooting
- 502/503 via CloudFront: check ALB target health, origin timeouts, and app/container health.
- Mixed content (HTTPS page → HTTP API): ensure the frontend points to an HTTPS API or serve both over the same scheme in dev.
- CORS 403 preflight: backend must allow the page origin; see `CorsConfig` and `application.properties` actuator CORS settings.
- CloudFront taking time: propagate and/or invalidate the distribution.

---
## Project Structure
- Backend (Spring Boot): `backend/`
- Frontend (React): `frontend/`
- Infrastructure (Terraform): `infra/terraform/`
- Docs: `archDesign.md`, `requirement.md`, `prodRequirement.md`, `tasks.md`

---
## Notes
- Never commit real credentials or Terraform state. See `.gitignore` and follow the security best practices above.
- For local development, you can run backend and frontend entirely on localhost without any AWS resources.
