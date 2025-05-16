# JWT Authentication Implementation

This project implements JWT (JSON Web Token) based authentication with access and refresh tokens for the casino application.

## Features

- User registration with email, username, and password
- User login with JWT token-based authentication
- Access Token and Refresh Token generation
- Token validation endpoint
- Token refresh functionality
- Protected API endpoints

## Technology Stack

- Spring Boot 3.x
- PostgreSQL
- JWT (JSON Web Token)
- Java 17+

## API Endpoints

### Authentication

- `POST /auth/register` - Register a new user
- `POST /auth/login` - Login with username and password
- `POST /auth/refresh-token` - Refresh an expired access token
- `POST /auth/validate-token` - Validate an access token

## Testing the Authentication

To test the authentication functionality, follow these steps:

1. Start the application using: `./mvnw spring-boot:run`
2. Open your browser and navigate to: `http://localhost:8081/auth-test.html`
3. Use the web interface to:
   - Register a new user
   - Login with the registered credentials
   - Validate your token
   - Refresh your token
   - Test protected endpoints

## Token Structure

The application uses two types of tokens:

1. **Access Token** - Short-lived token (1 hour by default) used for API access
2. **Refresh Token** - Long-lived token (30 days by default) used to obtain new access tokens

## Security Notes

This implementation uses:

- SHA-256 for password hashing (for demonstration purposes only)
- JWT HMAC-SHA512 (HS512) for token signing

In a production environment, consider:

- Using Spring Security with BCrypt for password hashing
- Implementing proper token revocation mechanisms
- Storing refresh tokens in a database
- Using more complex JWT claims for role-based authorization
- Implementing rate limiting and other security measures
