# Spring Boot GenAI Chatbot

An end-to-end GenAI chatbot built with **Spring Boot 3**, **PostgreSQL**, **JWT authentication**, and the **Google Gemini API**.

The application provides user registration and login, secure authentication, persistent chat history, and AI-generated responses with conversational context.

## Tech Stack

- **Java 17**
- **Spring Boot 3**
- **Spring MVC**
- **Spring Data JPA / Hibernate**
- **PostgreSQL**
- **JWT Authentication**
- **BCrypt Password Hashing**
- **Google Gemini API**
- **HTML, CSS, JavaScript**
- **Maven**

## Features

- User registration and login
- JWT-based authentication
- BCrypt password hashing
- PostgreSQL-backed user and chat history storage
- Google Gemini API integration
- Conversational context using recent chat history
- Server-side API key management
- Request validation
- Centralized exception handling
- API timeout handling
- Retry logic for temporary LLM failures
- Single-page web interface

## Architecture

```text
Browser
   |
   | REST API + JWT
   v
Spring Boot Application
   |
   +------ PostgreSQL
   |       |
   |       +-- Users
   |       +-- Chat History
   |
   +------ Google Gemini API
           |
           +-- AI-generated responses
```

The Gemini API key is stored on the server through an environment variable and is never exposed to the browser.

## Project Structure

```text
chatbot/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── sharad/
│       │           └── chatbot/
│       └── resources/
│           ├── static/
│           │   └── index.html
│           └── application.properties
├── .gitignore
├── pom.xml
└── README.md
```

## Run Locally

### Prerequisites

- Java 17+
- Maven
- PostgreSQL
- Google Gemini API key

### 1. Create the database

Create a PostgreSQL database named `chatbot`.

```sql
CREATE DATABASE chatbot;
```

### 2. Set environment variables

The application uses environment variables for sensitive configuration.

#### Windows PowerShell

```powershell
$env:DB_PASSWORD="your_postgres_password"
$env:GEMINI_API_KEY="your_gemini_api_key"
$env:JWT_SECRET="your_long_random_secret"
```

> Replace the placeholder values with your own credentials.
>
> Never commit your actual API key, database password, or JWT secret to GitHub.

### 3. Start the application

From the project directory:

```powershell
mvn spring-boot:run
```

Then open:

```text
http://localhost:8080
```

## Authentication Flow

```text
User
 |
 | Register / Login
 v
Spring Boot
 |
 | BCrypt password verification
 | JWT generation
 v
JWT Token
 |
 | Sent with authenticated requests
 v
Protected Chat APIs
```

The browser stores the JWT and sends it as a Bearer token when accessing protected endpoints.

## API Endpoints

| Method | Endpoint | Authentication | Description |
|---|---|---|---|
| POST | `/api/auth/register` | None | Register a new user |
| POST | `/api/auth/login` | None | Authenticate a user |
| POST | `/api/chat` | Bearer JWT | Send a message to the chatbot |
| GET | `/api/chat/history` | Bearer JWT | Retrieve chat history |

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

The response contains a JWT token that is used for authenticated requests.

### Chat

```http
POST /api/chat
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "message": "Hello, how are you?"
}
```

### Chat History

```http
GET /api/chat/history
Authorization: Bearer <JWT>
```

Returns the user's stored conversation history.

## Gemini API Integration

The application integrates directly with the **Google Gemini API** through a dedicated `LlmService`.

The service:

- Converts application messages into Gemini's `contents`, `role`, and `parts` request structure.
- Maps application `assistant` messages to Gemini's `model` role.
- Sends the Gemini API key using the server-side `x-goog-api-key` header.
- Sends recent conversation history to Gemini.
- Parses the generated response.
- Returns the AI response to the Spring Boot chat endpoint.

Current model:

`gemini-3.8-flash`

## Conversation Context

LLM APIs are stateless, so the application stores chat messages in PostgreSQL.

Before sending a new request to Gemini, the application retrieves recent messages and includes them in the request.

This allows the chatbot to maintain conversational context across messages.

The configured history limit is:

`10 messages`

## Security

The application implements several security measures:

- Passwords are hashed using BCrypt.
- JWT is used to protect authenticated endpoints.
- Gemini API credentials remain on the server.
- API keys and passwords are provided through environment variables.
- The Gemini API key is never exposed to the frontend.
- Login errors use generic messages to avoid revealing whether an email exists.
- Input validation is performed on API requests.

## Error Handling

The application uses centralized exception handling to provide consistent API error responses.

Example:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input"
  }
}
```

The application also handles failures from the Gemini API, including temporary rate limits and server-side errors.

## Reliability

The LLM integration includes:

- 5-second connection timeout
- 30-second read timeout
- Up to 3 request attempts
- Backoff between retry attempts
- Retry handling for HTTP 429 responses
- Retry handling for HTTP 5xx responses
- Timeout handling
- Empty-response handling

Chat history is saved only after a successful AI response.

## Database

PostgreSQL is used to persist:

- User accounts
- Password hashes
- Chat messages
- Conversation history

Hibernate/JPA handles database interaction and schema management.

## Environment Variables

| Variable | Purpose |
|---|---|
| `DB_PASSWORD` | PostgreSQL database password |
| `GEMINI_API_KEY` | Google Gemini API key |
| `JWT_SECRET` | Secret used for signing JWT tokens |

The application reads these values from the environment instead of hard-coding sensitive credentials.

## Design Decisions

### Server-side Gemini API key

The Gemini API key is kept in the Spring Boot backend instead of the browser.

```text
Browser
   |
   | JWT
   v
Spring Boot
   |
   | Gemini API Key
   v
Google Gemini API
```

This prevents the API key from being exposed to users through the frontend.

### Recent conversation history

The Gemini API does not automatically maintain application conversation state.

The application therefore stores messages in PostgreSQL and sends the most recent messages with each request.

### Separate LLM service

LLM communication is isolated inside `LlmService`.

This keeps the external AI integration separate from the controller and business logic and makes the application easier to maintain.

## What I Learned

This project provided hands-on experience with:

- Spring Boot REST API development
- JWT authentication
- BCrypt password hashing
- PostgreSQL and JPA/Hibernate
- REST API integration
- Google Gemini API integration
- LLM conversation context management
- Environment-based secret management
- Exception handling
- API timeout and retry strategies
- Frontend-backend communication
- Git and GitHub version control

## Future Improvements

Possible future improvements include:

- Streaming Gemini responses
- More advanced conversation management
- Message pagination
- User profile management
- Improved frontend UI
- Automated testing
- Docker-based deployment
- Production deployment