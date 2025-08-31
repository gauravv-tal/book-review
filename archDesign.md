# Book Review Platform – Architecture & Design Document

## 1. Architecture Overview
The platform follows a microservices-inspired, cloud-native architecture with clear separation between frontend, backend, and infrastructure components. The system is designed for scalability, maintainability, and secure operations on AWS.

## 2. High-Level Component Diagram
```mermaid
graph TD
  subgraph Frontend
    FE[ReactJS SPA]
  end
  subgraph Backend
    BE[Spring Boot API]
    DB[(PostgreSQL)]
    S3[S3 Bucket (Book Images)]
    SES[AWS SES (Email)]
    LLM[Perplexity Pro API]
  end
  subgraph Infrastructure
    TF[Terraform Scripts]
    CI[GitHub Actions (CI/CD)]
    CF[CloudFront]
  end
  FE -- REST API --> BE
  BE -- JDBC --> DB
  BE -- S3 URLs --> S3
  BE -- Email --> SES
  BE -- LLM Calls --> LLM
  FE -- Static Hosting --> S3
  S3 -- CDN --> CF
  CI -- Build/Deploy --> FE
  CI -- Build/Deploy --> BE
  TF -- Provision --> DB
  TF -- Provision --> S3
  TF -- Provision --> SES
  TF -- Provision --> CF
```

## 3. Tech Stack
- **Frontend:** ReactJS (SPA), hosted on AWS S3 + CloudFront
- **Backend:** Java (Spring Boot), packaged as Docker container
- **Database:** PostgreSQL (AWS RDS)
- **Storage:** AWS S3 (book cover images, import files)
- **Email:** AWS SES
- **LLM Recommendations:** Perplexity Pro API
- **API Documentation:** Swagger/OpenAPI
- **Infrastructure as Code:** Terraform
- **CI/CD:** GitHub Actions

## 4. Non-Functional Requirements
- **Security:**
  - JWT-based authentication (Spring Security)
  - Role-based access control (User, Admin, Moderator)
  - Secure storage for secrets (AWS Secrets Manager or SSM)
- **Scalability:**
  - Stateless backend, scalable via Docker/ECS/EKS
  - Frontend served via CDN (CloudFront)
- **Reliability & Availability:**
  - Automated testing, >80% code coverage
  - Monitoring/alerting (CloudWatch, logs)
- **Performance:**
  - Fast API responses, efficient DB queries, paginated endpoints
- **Maintainability:**
  - Modular codebase, clear API contracts, comprehensive docs
- **Cost Efficiency:**
  - Use AWS managed services, optimize resource allocation

## 5. Key Design Decisions
- **Spec-driven development:** All features are first specified in PRD/design/task breakdown before implementation.
- **Book import:** CSV/text file import, images referenced via S3 URLs.
- **LLM recommendations:** Integrate with Perplexity Pro API for personalized suggestions.
- **Deployment:** Automated via GitHub Actions, Dockerized backend, Terraform-managed AWS infra.
- **Separation of concerns:** Distinct repos for frontend and backend, clear API boundaries.

## 6. Role & Permission Model
- **User:** Can browse/search books, write/edit/delete own reviews, mark favourites, get recommendations.
- **Moderator:** Can moderate reviews and users, manage inappropriate content.
- **Admin:** Full access to all resources, user management, system settings.

## 7. Deployment Overview
- **Frontend:**
  - Built React app deployed to S3, served via CloudFront
  - CI/CD pipeline via GitHub Actions
- **Backend:**
  - Spring Boot app containerized with Docker
  - Deployed to AWS (ECS/EKS/EC2 as per Terraform config)
  - Uses RDS for data, S3 for images, SES for email
- **Infrastructure:**
  - Provisioned and managed via Terraform scripts
  - All secrets managed securely

## 8. Extensibility & Future Enhancements
- Add social features (comments, likes)
- Real-time notifications (WebSocket, SNS)
- Advanced analytics/dashboard for admins
- Multi-language/i18n support
