# User Module Implementation - HK Bank System

## Overview
Complete User module with JWT-based authentication and authorization, audit logging, and comprehensive security configuration.

## Module Structure

### 1. Entity Layer
- **`User`** (`az.hkbank.module.user.entity.User`)
  - Implements Spring Security `UserDetails`
  - Fields: id, firstName, lastName, email, password, phoneNumber, role, isDeleted, createdAt, updatedAt, version
  - Optimistic locking with `@Version`
  - Soft delete support
  - JPA lifecycle callbacks for timestamps

- **`Role`** (`az.hkbank.module.user.entity.Role`)
  - Enum: USER, ADMIN, AI_SUPPORT
  - Implements `GrantedAuthority` for Spring Security

- **`AuditLog`** (`az.hkbank.module.audit.entity.AuditLog`)
  - Tracks user actions: userId, action, description, ipAddress, createdAt

### 2. Repository Layer
- **`UserRepository`** (`az.hkbank.module.user.repository.UserRepository`)
  - `findByEmail(String)`, `findByPhoneNumber(String)`
  - `existsByEmail(String)`, `existsByPhoneNumber(String)`
  - Custom `findAll()` query - returns only non-deleted users

- **`AuditLogRepository`** (`az.hkbank.module.audit.repository.AuditLogRepository`)
  - `findByUserId(Long)`

### 3. DTO Layer
- **`RegisterRequest`** - firstName, lastName, email, password, phoneNumber (with validation)
- **`LoginRequest`** - email, password (with validation)
- **`AuthResponse`** - token, tokenType, email, firstName, lastName, role
- **`UserResponse`** - id, firstName, lastName, email, phoneNumber, role, createdAt
- **`UpdateUserRequest`** - firstName, lastName, phoneNumber (with validation)

### 4. Mapper Layer
- **`UserMapper`** (`az.hkbank.module.user.mapper.UserMapper`)
  - MapStruct interface
  - `User → UserResponse`
  - `RegisterRequest → User`

### 5. Service Layer
- **`UserService`** (interface)
  - `register(RegisterRequest)`, `login(LoginRequest)`
  - `getUserById(Long)`, `updateUser(Long, UpdateUserRequest)`
  - `softDeleteUser(Long)`, `getAllUsers()`

- **`UserServiceImpl`** (implementation)
  - Implements `UserService` and `UserDetailsService`
  - BCrypt password encoding
  - JWT token generation
  - Audit logging for all actions (LOGIN, REGISTER, USER_UPDATE, USER_DELETE)
  - IP address extraction from HttpServletRequest
  - Throws `BankException` with proper `ErrorCode`

- **`AuditService`** (`az.hkbank.module.audit.service.AuditService`)
  - `log(userId, action, description, ipAddress)`
  - Async logging with error handling

### 6. Security Configuration

#### JWT Infrastructure
- **`JwtProperties`** (`az.hkbank.config.jwt.JwtProperties`)
  - `@ConfigurationProperties(prefix = "app.jwt")`
  - Fields: secret, expiration

- **`JwtService`** (`az.hkbank.config.jwt.JwtService`)
  - `generateToken(UserDetails)`
  - `extractEmail(String)`, `extractExpiration(String)`
  - `isTokenValid(String, UserDetails)`
  - Uses JJWT 0.12.6 with HMAC-SHA signing

- **`JwtAuthFilter`** (`az.hkbank.config.jwt.JwtAuthFilter`)
  - Extends `OncePerRequestFilter`
  - Extracts Bearer token from Authorization header
  - Validates token and sets SecurityContext

#### Security Config
- **`SecurityConfig`** (`az.hkbank.config.SecurityConfig`)
  - `@EnableWebSecurity`, `@EnableMethodSecurity`
  - Public endpoints: `/api/auth/**`, `/actuator/health`, Swagger UI
  - Admin-only: `/api/admin/**`
  - Stateless session management
  - CSRF disabled (JWT-based)
  - BCryptPasswordEncoder bean

### 7. Controller Layer

#### AuthController
- **POST** `/api/auth/register` - Register new user (public)
- **POST** `/api/auth/login` - User login (public)

#### UserController
- **GET** `/api/users/me` - Get current user profile (authenticated)
- **PUT** `/api/users/me` - Update current user profile (authenticated)
- **DELETE** `/api/users/me` - Soft delete current user (authenticated)
- **GET** `/api/admin/users` - Get all users (ADMIN only)
- **GET** `/api/admin/users/{id}` - Get user by ID (ADMIN only)

All endpoints:
- Return `ApiResponse<T>` wrapper
- Include Swagger/OpenAPI annotations
- Proper HTTP status codes

### 8. Testing
- **`UserServiceTest`** (`az.hkbank.module.user.UserServiceTest`)
  - Uses Mockito with JUnit 5
  - Test cases:
    - `registerUser_Success`
    - `registerUser_EmailAlreadyExists_ThrowsBankException`
    - `registerUser_PhoneAlreadyExists_ThrowsBankException`
    - `login_Success`
    - `login_InvalidCredentials_ThrowsBankException`
    - `softDeleteUser_Success`
    - `softDeleteUser_UserNotFound_ThrowsBankException`

### 9. Configuration
- **`OpenApiConfig`** - Swagger/OpenAPI configuration with JWT security scheme

## Features Implemented

### Security Features
- JWT-based stateless authentication
- BCrypt password hashing
- Role-based access control (USER, ADMIN, AI_SUPPORT)
- Method-level security with `@PreAuthorize`
- Soft delete support for users

### Audit Features
- Comprehensive audit logging
- IP address tracking (supports X-Forwarded-For)
- Action constants: LOGIN, REGISTER, USER_UPDATE, USER_DELETE

### Error Handling
- Global exception handler with `@RestControllerAdvice`
- Consistent error responses using `ApiResponse<T>`
- Proper HTTP status codes from `ErrorCode` enum
- Validation error handling with field-level details

### Best Practices
- Clean Code principles
- SOLID principles
- Lombok for boilerplate reduction
- MapStruct for type-safe mapping
- Comprehensive Javadoc documentation
- No hardcoded strings (constants and enums)
- Optimistic locking for concurrent updates
- Transaction management

## API Endpoints Summary

### Public Endpoints
```
POST /api/auth/register - Register new user
POST /api/auth/login    - User login
GET  /actuator/health   - Health check
```

### Authenticated Endpoints
```
GET    /api/users/me - Get current user profile
PUT    /api/users/me - Update current user profile
DELETE /api/users/me - Delete current user account
```

### Admin Endpoints
```
GET /api/admin/users     - Get all users
GET /api/admin/users/{id} - Get user by ID
```

## Configuration Required

### application.yaml
```yaml
app:
  jwt:
    secret: <base64-encoded-secret>
    expiration: 86400000 # 24 hours in milliseconds
```

## Testing
Run unit tests:
```bash
mvn test
```

## Swagger UI
Access API documentation at:
```
http://localhost:8080/swagger-ui.html
```

## Database Schema

### users table
- id (BIGSERIAL PRIMARY KEY)
- first_name (VARCHAR NOT NULL)
- last_name (VARCHAR NOT NULL)
- email (VARCHAR UNIQUE NOT NULL)
- password (VARCHAR NOT NULL)
- phone_number (VARCHAR UNIQUE NOT NULL)
- role (VARCHAR NOT NULL)
- is_deleted (BOOLEAN DEFAULT false)
- created_at (TIMESTAMP NOT NULL)
- updated_at (TIMESTAMP NOT NULL)
- version (BIGINT)

### audit_logs table
- id (BIGSERIAL PRIMARY KEY)
- user_id (BIGINT NOT NULL)
- action (VARCHAR NOT NULL)
- description (VARCHAR(1000))
- ip_address (VARCHAR)
- created_at (TIMESTAMP NOT NULL)

## Next Steps
1. Run `mvn clean install` to compile and verify
2. Start the application
3. Test endpoints using Swagger UI or Postman
4. Create initial admin user if needed
