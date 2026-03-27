# 🏦 HK Bank System

[![CI](https://github.com/huseyinkarimli/hk-bank-system/actions/workflows/ci.yml/badge.svg)](https://github.com/huseyinkarimli/hk-bank-system/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Overview

Professional Digital Banking & Transaction Engine built with modern Java technologies. A comprehensive banking solution featuring multi-currency accounts, card management, real-time transactions, fraud detection, AI-powered customer support, and administrative tools.

## 🚀 Tech Stack

- **Backend**: Java 21, Spring Boot 3.5.11
- **Security**: Spring Security, JWT, BCrypt
- **Database**: PostgreSQL 16
- **ORM**: Hibernate (JPA)
- **Mapping**: MapStruct
- **Build**: Maven
- **Documentation**: Swagger/OpenAPI 3.0
- **Utilities**: Lombok, iText7 (PDF), Gson
- **AI Integration**: Google Gemini API
- **Monitoring**: Spring Actuator
- **Containerization**: Docker, Docker Compose

## ✨ Features

### Core Banking
- ✅ **User Authentication**: JWT-based secure authentication with role-based access control
- ✅ **Multi-currency Accounts**: Support for AZN, USD, EUR with automatic IBAN generation
- ✅ **Card Management**: Debit, credit, and virtual cards with PIN protection
- ✅ **Currency Exchange**: Real-time exchange rates with live API integration

### Transactions & Payments
- ✅ **P2P Transfers**: Card-to-card and IBAN-based transfers with validation
- ✅ **Utility Payments**: Mobile, internet, electricity, water, gas payments
- ✅ **Transaction Limits**: Daily and per-transaction limits with configurable thresholds
- ✅ **Fraud Detection**: Multi-layered fraud detection system with automatic blocking

### Advanced Features
- ✅ **AI Support**: Google Gemini-powered customer support chatbot in Azerbaijani
- ✅ **PDF Statements**: Professional account statements with transaction history
- ✅ **Notifications**: Real-time push notifications for all banking activities
- ✅ **Audit Logging**: Comprehensive audit trail for all system operations
- ✅ **Admin Panel**: Full administrative dashboard for user, account, and transaction management
- ✅ **Scheduled Tasks**: Automated daily limit resets and system health monitoring

## 📋 Prerequisites

- **Java 21** or higher
- **Docker** and **Docker Compose**
- **Maven 3.8+** (optional, wrapper included)

## 🚀 Sürətli Başlanğıc

### Tələblər
- Docker Desktop
- Git

### Başlatmaq
```bash
# 1. Layihəni klonla
git clone https://github.com/huseyinkarimli/hk-bank-system.git
cd hk-bank-system

# 2. .env faylını yarat
cp .env.example .env
# .env faylını açıb API açarlarını daxil et

# 3. Bir əmrlə başlat
docker-compose up -d --build

# 4. Hazır!
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080
# pgAdmin: http://localhost:5050
# Swagger: http://localhost:8080/swagger-ui.html
```

### Dayandırmaq
```bash
docker-compose down
```

### Logları izləmək
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
```

## 🛠️ Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/huseyinkarimli/hk-bank-system.git
cd hk-bank-system
```

### 2. Configure Environment

```bash
cp .env.example .env
```

Edit `.env` and configure:

```env
DB_USERNAME=hkbank_user
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_key_minimum_256_bits
EXCHANGE_API_KEY=your_exchangerate_api_key
GEMINI_API_KEY=your_gemini_api_key
```

### 3. Start Services

```bash
docker-compose up -d --build
```

This will start:
- PostgreSQL database on port 5432
- PgAdmin on port 5050
- Backend API on port 8080
- Frontend (nginx + React) on port 3000

### 4. Access Services

- **Frontend**: http://localhost:3000
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **PgAdmin**: http://localhost:5050

## 🏗️ Architecture

### Modular Monolith Structure

```
az.hkbank/
├── common/          # Shared utilities, exceptions, responses
├── config/          # Security, JWT, OpenAPI, Scheduling, AI
└── module/
    ├── user/        # User management & authentication
    ├── account/     # Account operations & statements
    ├── card/        # Card management
    ├── transaction/ # Money transfers & fraud detection
    ├── payment/     # Utility payments
    ├── notification/# Push notifications
    ├── audit/       # Audit logging
    ├── admin/       # Administrative operations
    └── ai/          # AI support chatbot
```

### Design Principles

- **Clean Code**: SOLID principles, meaningful naming, comprehensive documentation
- **Security First**: JWT authentication, role-based authorization, password encryption
- **API Design**: RESTful endpoints, consistent response wrapping, proper HTTP status codes
- **Error Handling**: Custom exceptions, localized error messages, detailed logging
- **Testing**: Comprehensive unit tests with 76+ test cases

## 📚 API Documentation

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login (returns JWT token)

### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update profile
- `DELETE /api/users` - Soft delete account

### Account Management
- `POST /api/accounts` - Create account (AZN, USD, EUR)
- `GET /api/accounts` - List user accounts
- `GET /api/accounts/{id}` - Get account details
- `GET /api/accounts/{id}/statement` - Get statement (JSON)
- `GET /api/accounts/{id}/statement/pdf` - Download PDF statement

### Card Management
- `POST /api/cards` - Create card
- `GET /api/cards` - List user cards
- `PUT /api/cards/{id}/block` - Block card
- `PUT /api/cards/{id}/unblock` - Unblock card
- `PUT /api/cards/{id}/freeze` - Freeze card
- `PUT /api/cards/{id}/unfreeze` - Unfreeze card

### Transactions
- `POST /api/transactions/card-transfer` - Card-to-card transfer
- `POST /api/transactions/iban-transfer` - IBAN transfer
- `GET /api/transactions` - List transactions
- `GET /api/transactions/{id}` - Get transaction details

### Payments
- `POST /api/payments` - Make utility payment
- `GET /api/payments` - List payments
- `GET /api/payments/{id}` - Get payment details

### Notifications
- `GET /api/notifications` - List notifications
- `GET /api/notifications/unread` - Unread notifications
- `PUT /api/notifications/{id}/read` - Mark as read
- `PUT /api/notifications/read-all` - Mark all as read

### AI Support
- `POST /api/ai/sessions` - Start chat session
- `POST /api/ai/sessions/{sessionId}/messages` - Send message
- `GET /api/ai/sessions` - List user sessions
- `GET /api/ai/sessions/{sessionId}/messages` - Get chat history
- `DELETE /api/ai/sessions/{sessionId}` - Close session

### Admin (ADMIN role required)
- `GET /api/admin/users` - List all users
- `PUT /api/admin/users/{id}/role` - Change user role
- `GET /api/admin/accounts` - List all accounts
- `GET /api/admin/transactions` - List all transactions
- `GET /api/admin/audit-logs` - View audit logs
- `GET /api/admin/system/health` - System health metrics
- `GET /api/admin/system/metrics` - System metrics
- `GET /api/admin/system/info` - System information

## 🧪 Testing

### Run Tests

```bash
cd backend
./mvnw test
```

### Test Coverage

- **76 tests** covering all modules
- Unit tests with Mockito
- H2 in-memory database for testing
- Comprehensive service layer testing

## 🐳 Docker Deployment

### Build and Run with Docker Compose

```bash
docker-compose up -d
```

### Build Backend Only

```bash
cd backend
docker build -t hkbank-backend .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/hkbank_db \
  -e DB_USERNAME=hkbank_user \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_jwt_secret \
  hkbank-backend
```

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication with configurable expiration
- **Password Encryption**: BCrypt with salt for secure password storage
- **Role-Based Access Control**: USER, ADMIN, AI_SUPPORT roles
- **Method-Level Security**: `@PreAuthorize` annotations for fine-grained control
- **Fraud Detection**: Real-time fraud analysis with configurable thresholds
- **Audit Logging**: Complete audit trail of all system operations

## 📊 Database Schema

### Core Tables
- `users` - User accounts with credentials
- `accounts` - Bank accounts (multi-currency)
- `cards` - Debit/Credit/Virtual cards
- `transactions` - Money transfers
- `payments` - Utility payments
- `notifications` - User notifications
- `audit_logs` - System audit trail
- `chat_sessions` - AI chat sessions
- `chat_messages` - AI conversation history

## 🔧 Development

### Local Development Setup

```bash
# Start only database
docker-compose up postgres -d

# Run application in local profile
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Environment Profiles

- **local**: H2 in-memory database, debug logging
- **dev**: PostgreSQL, debug logging, Actuator enabled
- **prod**: PostgreSQL, info logging, minimal Actuator

## 📝 Configuration

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/hkbank_db` |
| `DB_USERNAME` | Database username | `hkbank_user` |
| `DB_PASSWORD` | Database password | `secure_password` |
| `JWT_SECRET` | JWT signing key (256+ bits) | `your_secret_key` |
| `EXCHANGE_API_KEY` | Currency exchange API key | `your_api_key` |
| `GEMINI_API_KEY` | Google Gemini API key | `your_gemini_key` |

### Getting API Keys

- **Exchange Rate API**: Sign up at [exchangerate-api.com](https://www.exchangerate-api.com/)
- **Google Gemini API**: Get key from [Google AI Studio](https://makersuite.google.com/app/apikey)

## 🤖 AI Support

The system includes an AI-powered customer support chatbot using Google Gemini:

- Natural language processing in Azerbaijani
- Context-aware responses based on conversation history
- Bank-specific knowledge about accounts, cards, and transactions
- Secure, user-specific chat sessions
- Automatic title generation from first message

## 📈 Monitoring

### Actuator Endpoints (ADMIN only)

- `/actuator/health` - Application health status
- `/actuator/metrics` - System metrics
- `/actuator/info` - Application information

### Scheduled Tasks

- **Daily 00:00**: Reset transaction limits
- **Hourly**: System health check and logging

## 🧪 CI/CD

GitHub Actions workflow automatically:
- Runs all tests on push/PR
- Builds Docker image on main branch
- Pushes to GitHub Container Registry
- Caches Maven dependencies for faster builds

## 📦 Modules Overview

| Module | Description | Key Features |
|--------|-------------|--------------|
| **User** | Authentication & profile | Registration, login, JWT, role management |
| **Account** | Bank accounts | Multi-currency, IBAN generation, balance management |
| **Card** | Card operations | PIN protection, status management, limits |
| **Transaction** | Money transfers | P2P, fraud detection, daily limits |
| **Payment** | Utility payments | Multiple providers, validation, history |
| **Notification** | Push notifications | Real-time alerts, read status tracking |
| **Audit** | Audit logging | Complete activity trail, entity tracking |
| **AI** | Customer support | Gemini-powered chatbot, session management |
| **Admin** | Administration | User management, system monitoring, statistics |

## 🛡️ Error Handling

All errors return standardized responses with:
- HTTP status code
- Error code enum
- Localized message (Azerbaijani)
- Timestamp

Example error response:

```json
{
  "success": false,
  "message": "Balans kifayət deyil",
  "errorCode": "INSUFFICIENT_BALANCE",
  "timestamp": "2026-03-17T00:00:00"
}
```

## 🤝 Contributing

This is a portfolio project. For questions or suggestions, please reach out.

## 👤 Author

**Huseyin Karimli**
- Email: huseyinkarimli.tech@gmail.com
- GitHub: [@huseyinkarimli](https://github.com/huseyinkarimli)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Google Gemini for AI capabilities
- iText for PDF generation
- PostgreSQL community

---

**Built with ❤️ by Huseyin Karimli**
