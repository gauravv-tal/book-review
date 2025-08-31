# Task Breakdown – Book Review Platform (End-to-End Pipeline First)

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
