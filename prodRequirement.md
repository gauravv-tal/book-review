# Book Review Platform – Product Requirements Document (PRD)

## 1. Overview
A minimal yet functional platform for users to discover books, write and manage reviews, rate books, and receive personalized recommendations. The system will include user authentication, CRUD operations for reviews, rating aggregation, and AI-powered recommendations.

## 2. Goals
- Enable users to browse, search, and discover books.
- Allow users to create, read, update, and delete book reviews.
- Provide an aggregated rating and review count for each book.
- Offer personalized book recommendations using LLM-based APIs (Perplexity Pro).
- Support user roles: User, Admin, Moderator.
- Ensure secure, scalable, and cloud-native deployment on AWS.
- Achieve high code quality with >80% unit test coverage.

## 3. Target Users
- **General Readers:** Users seeking book recommendations and reviews.
- **Book Enthusiasts:** Users who want to share their opinions and rate books.
- **Moderators/Admins:** Users responsible for managing content and users.

## 4. Functional Requirements
### 4.1 User Authentication & Roles
- Signup, login, logout using email/password.
- JWT-based authentication.
- Roles: User, Admin, Moderator.

### 4.2 Book Management
- Import book catalog from a CSV/text file (with S3 URLs for cover images).
- Display paginated, searchable book listings.
- Book details: title, author, description, cover image, genres, published year.

### 4.3 Reviews & Ratings
- Users can create, edit, delete their own reviews.
- Reviews include text, 1–5 star rating, timestamp.
- Only one review per user per book.
- Average book rating (rounded to 1 decimal) and total review count, auto-updated.

### 4.4 User Profile
- View list of own reviews.
- Mark/unmark favourite books.

### 4.5 Recommendations
- Personalized recommendations using Perplexity Pro API.
- Default: Top-rated books.
- Optionally, recommend based on user’s favourites/genres.

### 4.6 Admin/Moderator Features
- Manage books, users, and reviews.
- Moderate inappropriate content.

### 4.7 Email Notifications
- Use AWS SES for notifications (e.g., signup confirmation).

### 4.8 API Documentation
- Swagger/OpenAPI docs for all backend endpoints.

### 4.9 CI/CD and Deployment
- Automated build, test, and deployment via GitHub Actions.
- Backend deployed as Docker containers on AWS (ECS/EKS/EC2).
- Frontend (React) deployed to S3 + CloudFront.
- Infrastructure managed with Terraform.

## 5. Constraints
- Only use AI assistants (Copilot/Cursor/Windsurf/Kiro) for development.
- Must use Java (Spring Boot) for backend, ReactJS for frontend.
- Deployment and infra on AWS (via Terraform).
- Book data imported from file; images via S3 URLs.
- >80% backend code coverage required.
- All code, docs, and prompts must be publicly available in separate frontend/backend repos.

## 6. Non-Functional Requirements
- **Security:** Secure authentication, role-based access, input validation.
- **Scalability:** Cloud-native design, scalable infra (AWS).
- **Reliability:** Automated testing, monitoring, error handling.
- **Performance:** Fast API responses, efficient pagination and search.
- **Maintainability:** Well-documented code, modular design, clear API docs.
- **Usability:** Clean, intuitive UI for all user roles.

## 7. Out of Scope
- Social features (comments, likes, sharing)
- Real-time chat or notifications
- Payment or e-commerce features
