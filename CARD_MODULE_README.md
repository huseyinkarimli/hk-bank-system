# Card Module Implementation - HK Bank System

## Overview
Complete implementation of the Card module (Mərhələ 3) for HK Bank System, providing card management functionality with security, validation, and audit logging.

## Architecture

### Package Structure
```
az.hkbank.module.card
├── entity/
│   ├── Card.java
│   ├── CardStatus.java (enum)
│   └── CardType.java (enum)
├── repository/
│   └── CardRepository.java
├── dto/
│   ├── CreateCardRequest.java
│   ├── CardResponse.java
│   ├── CardSummaryResponse.java
│   ├── UpdateCardStatusRequest.java
│   └── ChangePinRequest.java
├── mapper/
│   └── CardMapper.java
├── service/
│   ├── CardService.java (interface)
│   └── impl/
│       └── CardServiceImpl.java
└── controller/
    └── CardController.java
```

## Key Features

### 1. Card Entity
- **Fields**: id, cardNumber (16 digits), cardHolder, expiryDate, cvv (encrypted), pin (encrypted), cardType, status, account (ManyToOne), isDeleted, createdAt, updatedAt, version
- **Security**: CVV and PIN are BCrypt encrypted, never returned in responses
- **Locking**: Optimistic locking with `@Version` field
- **Soft Delete**: Uses `isDeleted` flag for logical deletion

### 2. Card Number Generation
- **BIN**: Fixed "44222007" (first 8 digits)
- **Format**: 16 digits total, validated with Luhn algorithm
- **Uniqueness**: Verified against existing card numbers
- **Masking**: Cards displayed as "4422 **** **** 5678"
- **CVV**: 3 random digits, BCrypt encrypted
- **PIN**: 4 random digits, BCrypt encrypted

### 3. Business Rules
- Maximum 3 active cards per account
- Cards can only be created for ACTIVE accounts
- Card holder name is auto-generated from user's first and last name
- Expiry date is automatically set to 3 years from creation
- Card numbers follow Luhn algorithm validation
- PIN change requires current PIN verification
- Soft deletion blocks the card automatically

### 4. Security
- JWT authentication required for all endpoints
- User can only access their own cards
- Admin endpoints require ADMIN role
- CVV and PIN never exposed in API responses
- Card numbers always masked in responses
- Audit logging for all critical operations

### 5. Audit Actions
- `CARD_CREATED`: New card creation
- `CARD_BLOCKED`: Card blocked by user or admin
- `CARD_FROZEN`: Card frozen by user or admin
- `CARD_ACTIVATED`: Card activated
- `CARD_PIN_CHANGED`: PIN changed successfully
- `CARD_DELETED`: Card soft deleted

## API Endpoints

### User Endpoints (Authenticated)

#### POST /api/cards
Create a new card for an account.

**Request:**
```json
{
  "accountId": 1,
  "cardType": "DEBIT"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Card created successfully",
  "data": {
    "id": 1,
    "maskedCardNumber": "4422 **** **** 5678",
    "cardHolder": "HUSEYIN KARIMLI",
    "expiryDate": "2029-03-16",
    "cardType": "DEBIT",
    "status": "ACTIVE",
    "accountId": 1,
    "createdAt": "2026-03-16T14:30:00"
  },
  "timestamp": "2026-03-16T14:30:00"
}
```

#### GET /api/cards
Get all cards for the authenticated user.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "maskedCardNumber": "4422 **** **** 5678",
      "cardHolder": "HUSEYIN KARIMLI",
      "cardType": "DEBIT",
      "status": "ACTIVE",
      "expiryDate": "2029-03-16"
    }
  ],
  "timestamp": "2026-03-16T14:30:00"
}
```

#### GET /api/cards/{id}
Get card details by ID.

**Response:** Same as CardResponse above

#### GET /api/cards/account/{accountId}
Get all cards for a specific account.

**Response:** Array of CardSummaryResponse

#### PUT /api/cards/{id}/status
Update card status (ACTIVE/BLOCKED/FROZEN).

**Request:**
```json
{
  "status": "FROZEN",
  "reason": "User requested freeze"
}
```

**Response:** CardResponse with updated status

#### PUT /api/cards/{id}/pin
Change card PIN.

**Request:**
```json
{
  "currentPin": "1234",
  "newPin": "5678"
}
```

**Response:**
```json
{
  "success": true,
  "message": "PIN changed successfully",
  "data": null,
  "timestamp": "2026-03-16T14:30:00"
}
```

#### DELETE /api/cards/{id}
Soft delete a card.

**Response:**
```json
{
  "success": true,
  "message": "Card deleted successfully",
  "data": null,
  "timestamp": "2026-03-16T14:30:00"
}
```

### Admin Endpoints (ADMIN Role Required)

#### GET /api/cards/admin/all
Get all active cards in the system.

**Response:** Array of CardSummaryResponse

#### PUT /api/cards/admin/{id}/status
Update card status (admin operation).

**Request:**
```json
{
  "status": "BLOCKED",
  "reason": "Fraud detected"
}
```

**Response:** CardResponse with updated status

## Error Codes

New error codes added for Card module:
- `CARD_NOT_FOUND` (404): Card not found
- `CARD_BLOCKED` (403): Card is blocked
- `CARD_FROZEN` (403): Card is frozen
- `CARD_LIMIT_EXCEEDED` (400): Card limit exceeded for this account
- `INVALID_PIN` (401): Invalid PIN
- `UNAUTHORIZED_CARD_ACCESS` (403): Unauthorized access to card

## Testing

### Unit Tests (CardServiceTest)
All 9 tests passing:
1. ✅ `createCard_Success_Debit` - Creates card successfully
2. ✅ `createCard_CardLimitExceeded_ThrowsBankException` - Validates 3-card limit
3. ✅ `createCard_AccountNotFound_ThrowsBankException` - Validates account existence
4. ✅ `getCardById_Success` - Retrieves card by ID
5. ✅ `getCardById_NotOwner_ThrowsBankException` - Validates ownership
6. ✅ `getUserCards_Success` - Retrieves all user cards
7. ✅ `changePin_Success` - Changes PIN successfully
8. ✅ `changePin_InvalidPin_ThrowsBankException` - Validates current PIN
9. ✅ `softDeleteCard_Success` - Soft deletes card

### Integration Testing
- ✅ Application starts successfully with H2 database
- ✅ All endpoints accessible and functional
- ✅ JWT authentication working
- ✅ Card creation with auto-generated card number, CVV, PIN
- ✅ Card number masking working correctly
- ✅ Status updates (FROZEN) working
- ✅ Audit logging capturing all actions

## Database Schema

### cards Table
```sql
CREATE TABLE cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    card_number VARCHAR(16) NOT NULL UNIQUE,
    card_holder VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv VARCHAR(255) NOT NULL,
    pin VARCHAR(255) NOT NULL,
    card_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    account_id BIGINT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);
```

## Security Considerations

1. **Encryption**: CVV and PIN are BCrypt encrypted before storage
2. **Masking**: Card numbers always masked in responses (4422 **** **** 5678)
3. **No Exposure**: CVV and PIN never returned in any API response
4. **Authorization**: Users can only access their own cards
5. **Audit Trail**: All card operations logged with IP address
6. **Soft Delete**: Cards are never physically deleted from database

## Dependencies

All dependencies already present in `pom.xml`:
- Spring Boot 3.5.11
- Spring Security
- Spring Data JPA
- Lombok
- MapStruct
- BCrypt (via Spring Security)
- H2 Database (local/test)
- PostgreSQL (production)

## Configuration

No additional configuration required. Uses existing:
- JWT configuration from `application.yaml`
- Database configuration from profiles
- Security configuration from `SecurityConfig.java`
- Password encoder from `PasswordEncoderConfig.java`

## Usage Example

```bash
# 1. Register user
POST /api/auth/register
{
  "firstName": "Huseyin",
  "lastName": "Karimli",
  "email": "huseyinkarimli.tech@gmail.com",
  "password": "SecurePass123!",
  "phoneNumber": "+994501234567"
}

# 2. Create account
POST /api/accounts
Authorization: Bearer {token}
{
  "currencyType": "AZN"
}

# 3. Create card
POST /api/cards
Authorization: Bearer {token}
{
  "accountId": 1,
  "cardType": "DEBIT"
}

# 4. Get all cards
GET /api/cards
Authorization: Bearer {token}

# 5. Freeze card
PUT /api/cards/1/status
Authorization: Bearer {token}
{
  "status": "FROZEN",
  "reason": "Lost card"
}

# 6. Change PIN
PUT /api/cards/1/pin
Authorization: Bearer {token}
{
  "currentPin": "1234",
  "newPin": "5678"
}
```

## Test Results

**Total Tests**: 25 (1 Application + 8 Account + 9 Card + 7 User)
**Status**: ✅ All tests passing
**Coverage**: Service layer business logic fully tested

## Swagger Documentation

Access Swagger UI at: `http://localhost:8080/swagger-ui/index.html`

All Card endpoints documented with:
- Operation descriptions
- Request/response schemas
- HTTP status codes
- Authentication requirements
- Example payloads

## Next Steps

The Card module is complete and ready for:
- Transaction module integration (Mərhələ 4)
- Payment processing
- Card usage limits and restrictions
- Fraud detection integration
- Real-time card blocking/unblocking
- Card statement generation

---

**Implementation Date**: March 16, 2026
**Status**: ✅ COMPLETE
**Test Status**: ✅ ALL PASSING (25/25)
**Application Status**: ✅ RUNNING SUCCESSFULLY
