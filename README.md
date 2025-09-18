# Deployed dockerized IDP app
https://idp-auth-app.onrender.com

# Deployment app link
https://main-app-frontend.vercel.app/


# Identity Provider Service

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13%2B-blue.svg)](https://www.postgresql.org/)

A secure, scalable, and production-ready Identity Provider service built with Spring Boot 3, Spring Security, and JWT authentication. This service provides user registration, email verification, authentication, and OAuth2 integration with LinkedIn.

## Features

- JWT-based authentication with access and refresh tokens
- User registration with email verification
- Flyway for database migrations
- Email notifications for account verification
- Token refresh mechanism
- OAuth2 integration with LinkedIn
- Secure password hashing with BCrypt
- Role-based access control (RBAC)
- Containerized with Docker
- Ready for cloud deployment

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL 13+
- Git
- Flyway (for database migrations)
- Docker (for containerization)
- SMTP server (e.g., Gmail) for email notifications
- LinkedIn OAuth2 application credentials


### Running the Application

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/Identity-Provider.git
   cd Identity-Provider
   ```

2. **Build the application**
   ```bash
   ./mvnw clean package
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

The application will be available at `http://localhost:8080`

### Docker

Build and run the application using Docker:

```bash
docker build -t identity-provider .
docker run -p 8080:8080 --env-file .env identity-provider
```

## API Documentation

### Authentication Endpoints

#### Register a new user
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123!",
  "role": "USER"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123!"
}
```

#### Verify Email with OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "123456"
}
```

#### Refresh Token
```http
POST /api/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "your_refresh_token_here"
}
```

### LinkedIn OAuth2 Flow

1. **Initiate LinkedIn Login**
   ```
   GET /linkedin/authorize
   ```

2. **LinkedIn Callback**
   ```
   GET /linkedin/callback?code={authorization_code}
   ```

## Security

- JWT-based stateless authentication
- Password hashing with BCrypt
- Role-based access control
- CSRF protection
- CORS configuration
- Secure session management
- Input validation
- Rate limiting (recommended for production)

## Testing

Run the test suite:

```bash
./mvnw test
```

## Deployment

### Prerequisites

- Docker and Docker Compose
- Kubernetes (for production)

### Docker Compose

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: idp_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: your_secure_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    depends_on:
      - postgres
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/idp_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=your_secure_password
      # Add other environment variables from .env
    ports:
      - "8080:8080"

volumes:
  postgres_data:
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Contact
noelmugisha332@gmail.com
