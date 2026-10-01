# Spring Boot GenAI Chatbot

Spring Boot 3 + PostgreSQL + JWT + any OpenAI-compatible LLM API. One HTML page.

## Run
1. Java 17+, Maven, PostgreSQL. Create the DB: `createdb chatbot`
2. Set env vars (only the key is needed for real answers; blank = mock replies):
   ```
   export DB_PASSWORD=yourpgpassword
   export LLM_API_KEY=your_key
   # Optional providers (all OpenAI-compatible):
   # Groq:   LLM_BASE_URL=https://api.groq.com/openai/v1  LLM_MODEL=llama-3.1-8b-instant
   # Gemini: LLM_BASE_URL=https://generativelanguage.googleapis.com/v1beta/openai  LLM_MODEL=gemini-2.0-flash
   ```
3. `mvn spring-boot:run` then open http://localhost:8080

## API
| Method | Path | Auth | Body |
|---|---|---|---|
| POST | /api/auth/register | no | `{email,password}` |
| POST | /api/auth/login | no | `{email,password}` |
| POST | /api/chat | Bearer JWT | `{message}` |
| GET | /api/chat/history | Bearer JWT | |

Errors always look like `{"error":{"code":"...","message":"..."}}`.

## Design decisions
- API key stays on the server (LlmService); browser only holds a JWT.
- LLM API is stateless, so the last 10 messages are resent each call (history trimming).
- `max_tokens` caps answer cost; 5s connect / 30s read timeouts; 3 attempts with backoff on 429, 5xx and timeouts.
- History saved only after a successful LLM reply; no DB transaction held during the HTTP call.
- Passwords are BCrypt-hashed; login errors don't reveal whether an email exists.
- System prompt + user text in separate roles (basic prompt-injection hygiene, not a full fix).
