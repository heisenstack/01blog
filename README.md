# 01Blog

A modern, full-stack blogging platform built with Angular and Spring Boot. 01Blog enables users to create, share, and discover engaging content with features like user authentication, post management, commenting, notifications, and admin moderation tools.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Database](#database)
- [Contributing](#contributing)

## 🎯 Overview

01Blog is a comprehensive blogging platform designed for content creators and readers. It provides a seamless experience for publishing posts with media attachments, engaging with other users through comments and likes, and discovering new content through personalized feeds and user suggestions.

### Key Capabilities

- **User Management**: Registration, authentication, and profile management
- **Content Creation**: Rich post creation with support for multiple media files (images and videos)
- **Social Features**: Follow/unfollow users, personalized feeds, and user discovery
- **Engagement**: Like posts, comment on content, and receive notifications
- **Moderation**: Admin dashboard for content and user management, report handling
- **Real-time Notifications**: Stay updated with activities and new content

## ✨ Features

### User Features

- 🔐 Secure authentication with JWT tokens
- 👤 User profiles with follower/following statistics
- 📝 Create and edit blog posts with rich content
- 🖼️ Upload multiple media files (up to 5 per post) - images and videos
- ❤️ Like and unlike posts
- 💬 Comment on posts in real-time
- 🔍 Discover new users and content
- 🔔 Real-time notification system
- 📰 Personalized feed based on followed users
- 🚨 Report inappropriate content or users

### Admin Features

- 📊 Dashboard with platform statistics
- 👥 User management and moderation
- 📋 Post management and visibility control
- 🚩 Content report review and handling
- 🚫 User ban/unban capabilities
- 👁️ Hide/unhide posts

## 🛠️ Tech Stack

### Frontend

- **Framework**: Angular 20.3.2 (Standalone Components)
- **Language**: TypeScript
- **Styling**: SCSS with CSS Variables
- **HTTP Client**: Angular HttpClient
- **Notifications**: ngx-toastr
- **UI Icons**: Font Awesome 6.5.2
- **State Management**: RxJS Observables

### Backend

- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Database**: PostgreSQL
- **ORM**: Jakarta Persistence (JPA)
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **File Storage**: Local filesystem with validation

### DevOps & Tools

- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **Version Control**: Git

## 📦 Prerequisites

### System Requirements

- Node.js 22+ and npm
- Java 21+
- Maven 3.8+
- Docker & Docker Compose (for containerized setup)
- PostgreSQL 17 (if running without Docker)

### Environment Configuration

Create a `.env` file in the project root:

```env
# Database Configuration
POSTGRES_DB=zerooneblog
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password
POSTGRES_PORT=5432

# Backend Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/zerooneblog
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_secure_password

# Frontend Configuration
API_URL=http://localhost:8080/api
```

## 💾 Installation

### Clone the Repository

```bash
git clone https://github.com/heisenstack/01blog.git
cd 01blog
```

### Install Frontend Dependencies

```bash
cd frontend
npm install
cd ..
```

### Install Backend Dependencies

```bash
cd backend
mvn clean install
cd ..
```

## 🚀 Running the Application

### Option 1: Running with Docker Compose (Recommended)

Docker Compose simplifies the setup by orchestrating all services in containers.

#### Prerequisites

- Docker & Docker Compose installed

#### Steps

1. **Build and start all services:**

```bash
docker-compose up -d
```

2. **Verify services are running:**

```bash
docker-compose ps
```

Expected output:

```
CONTAINER ID   IMAGE                   PORTS
<id>          01blog-backend         0.0.0.0:8080->8080/tcp
<id>          01blog-frontend        0.0.0.0:4200->4200/tcp
<id>          postgres:17-alpine     0.0.0.0:5432->5432/tcp
```

3. **Access the application:**

   - Frontend: `http://localhost:4200`
   - Backend API: `http://localhost:8080/api`

4. **Stop services:**

```bash
docker-compose down
```

5. **View logs:**

```bash
docker-compose logs -f
# For specific service:
docker-compose logs -f backend
docker-compose logs -f frontend
```

### Option 2: Running Without Docker

#### Step 1: Start PostgreSQL Database

```bash
docker run -d \
  --name blog-db \
  -e POSTGRES_DB=zerooneblog \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  postgres:17-alpine
```

Or use a local PostgreSQL installation:

```bash
# macOS with Homebrew
brew services start postgresql

# Ubuntu/Debian
sudo systemctl start postgresql

# Windows
net start postgresql-x64-15
```

#### Step 2: Start the Backend

```bash
cd backend

# Load environment variables
export $(cat ../.env | grep -v '^#' | grep -v '^$' | xargs)

# Run Spring Boot application
mvn spring-boot:run
```

Backend will be available at: `http://localhost:8080`

**Initial Admin Credentials** (created automatically):

- Username: `admin`
- Password: `Admin123`

#### Step 3: Start the Frontend

In a new terminal:

```bash
cd frontend
npm install  # if not done already
ng serve
```

Frontend will be available at: `http://localhost:4200`

The application will automatically reload when you modify source files.

## 📁 Project Structure

```
01blog/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/zerooneblog/api/
│   │   │   │   ├── domain/              # Entity models
│   │   │   │   ├── service/             # Business logic
│   │   │   │   ├── infrastructure/      # Repositories
│   │   │   │   ├── interfaces/          # DTOs & Controllers
│   │   │   │   └── ZerooneblogApplication.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── auth/                    # Authentication components
│   │   │   ├── pages/                   # Page components
│   │   │   ├── components/              # Reusable components
│   │   │   ├── services/                # API services
│   │   │   ├── models/                  # TypeScript interfaces
│   │   │   ├── admin-dashboard/         # Admin panel
│   │   │   └── app.routes.ts
│   │   ├── index.html
│   │   └── main.ts
│   ├── package.json
│   ├── angular.json
│   └── Dockerfile
├── docker-compose.yml
├── .env
└── README.md
```

## 🔌 API Documentation

### Authentication Endpoints

```
POST /api/auth/signup          # User registration
POST /api/auth/login           # User login
GET  /api/auth/verify          # Token verification
POST /api/auth/logout          # User logout
```

### Post Endpoints

```
GET    /api/posts                 # Get all posts (paginated)
POST   /api/posts                 # Create new post
GET    /api/posts/{id}            # Get post details
PUT    /api/posts/{id}            # Update post
DELETE /api/posts/{id}            # Delete post
POST   /api/posts/{id}/like        # Like a post
DELETE /api/posts/{id}/like        # Unlike a post
POST   /api/posts/{id}/report      # Report a post
GET    /api/posts/feed             # Get personalized feed
```

### User Endpoints

```
GET    /api/users/{username}       # Get user profile
POST   /api/users/{username}/follow # Follow user
DELETE /api/users/{username}/follow # Unfollow user
POST   /api/users/{username}/report # Report user
```

### Comment Endpoints

```
POST   /api/comments               # Create comment
PUT    /api/comments/{id}          # Update comment
GET    /api/comments               # Get comments (paginated)
DELETE /api/comments/{id}          # Delete comment
```

### Admin Endpoints

```
GET    /api/admin/dashboard        # Dashboard statistics
GET    /api/admin/users            # Get all users (paginated)
GET    /api/admin/reports          # Get reports (paginated)
POST   /api/admin/posts/{id}/hide   # Hide post
POST   /api/admin/posts/{id}/unhide # Unhide post
DELETE /api/admin/posts/{id}        # Delete post
POST   /api/admin/users/{id}/ban    # Ban user
POST   /api/admin/users/{id}/unban  # Unban user
```

## 💾 Database

### Schema Overview

**Main Tables:**

- `users` - User accounts and authentication
- `posts` - Blog posts
- `post_media` - Media files attached to posts
- `comments` - Post comments
- `post_likes` - User likes on posts
- `user_followers` - Follow relationships
- `reports` - Content and user reports
- `notifications` - User notifications

### Database Configuration

The backend uses PostgreSQL with automatic schema initialization via Spring Boot JPA.

**Connection Details:**

- Host: `localhost` (without Docker) or `blog-db` (with Docker Compose)
- Port: `5432`
- Database: `zerooneblog`
- Username: `postgres`
- Password: configured in `.env`

### Accessing the Database

```bash
# Using psql
psql -h localhost -U postgres -d zerooneblog

# Using Docker (if running containerized database)
docker exec -it blog-db psql -U postgres -d zerooneblog
```

## 🔐 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Password Encryption**: Passwords hashed using Spring Security
- **File Upload Validation**: Whitelist of allowed media types and extensions
- **CORS Configuration**: Controlled cross-origin requests
- **Role-based Access Control**: Admin and User roles
- **Input Validation**: Server-side validation of all inputs
- **Report System**: Content moderation and user reporting

## 📝 File Upload Specifications

### Supported Media Types

**Images:**

- JPEG, PNG, GIF, WebP
- Maximum size: 10MB
- Maximum files per post: 5

**Videos:**

- MP4, WebM, MOV, AVI
- Maximum size: 50MB
- Maximum files per post: 5

## 🚀 Building for Production

### Build Frontend

```bash
cd frontend
ng build --configuration production
```

Production build output: `frontend/dist/`

### Build Backend

```bash
cd backend
mvn clean package -DskipTests
```

Production JAR: `backend/target/01blog-api.jar`

## 📚 Additional Resources

- [Angular Documentation](https://angular.io/docs)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

For issues, questions, or suggestions, please open an issue on the GitHub repository.

---

**Last Updated:** 2026
