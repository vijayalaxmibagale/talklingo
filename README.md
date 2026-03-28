# TalkLingo Live

TalkLingo Live is a full-stack multilingual communication platform for real-time translated conversations. It combines a Spring Boot backend, a React frontend, and Oracle-ready persistence so users can create protected language rooms, exchange translated messages, capture voice input, and replay translated output.

## Stack

- Frontend: React + Vite + STOMP/SockJS
- Backend: Java 17 + Spring Boot + Spring Security + WebSocket
- Database: Oracle Database
- Auth: JWT-based login/signup

## Features

- User registration and login with JWT authentication
- User-scoped conversation sessions
- Real-time translated transcript updates with WebSocket broadcasting
- Mock translation pipeline that returns translated text and audio URLs
- Workspace analytics dashboard
- Browser voice capture and speech playback for demos
- Responsive UI for mobile, tablet, and desktop

## Project structure

- `frontend/` React client
- `backend/` Spring Boot API
- `database/` Oracle schema and sample seed data

## Run locally

### 1. Oracle database

Create the schema with the SQL in [schema.sql](C:\Users\Shree\Downloads\backend\database\schema.sql) and optional sample data in [seed.sql](C:\Users\Shree\Downloads\backend\database\seed.sql).

### 2. Backend

```powershell
cd C:\Users\Shree\Downloads\backend\backend
.\mvnw.cmd spring-boot:run
```

The API runs on `http://localhost:8080`.

Environment variables supported by the backend:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT`
- `MEDIA_BASE_URL`
- `TALKLINGO_TRANSLATION_PROVIDER`
- `GOOGLE_TRANSLATE_API_KEY`
- `TALKLINGO_SPEECH_PROVIDER`
- `GOOGLE_SPEECH_BASE_URL`

### 3. Frontend

```powershell
cd C:\Users\Shree\Downloads\backend\frontend
npm install
npm run dev
```

The client runs on `http://localhost:5173`.

Optional frontend variable:

- `VITE_API_BASE_URL`

## Demo flow

1. Register a new user or sign in.
2. Create a session like `English to Hindi Customer Support`.
3. Use voice capture or type a message. The browser microphone uses the session source language when speech recognition is supported.
4. Broadcast the translated output.
5. Replay the translated message and watch live transcript updates.

## API highlights

- `POST /api/users/register`
- `POST /api/users/login`
- `GET /api/users/profile`
- `GET /api/sessions`
- `GET /api/sessions/stats`
- `POST /api/sessions`
- `GET /api/sessions/{sessionId}`
- `POST /api/sessions/{sessionId}/messages`

## Notes

- The current translation provider is intentionally mocked so the project works during a hackathon without paid AI services.
- Set `TALKLINGO_TRANSLATION_PROVIDER=google`, `TALKLINGO_SPEECH_PROVIDER=google`, and `GOOGLE_TRANSLATE_API_KEY` to enable Google Cloud speech-to-text plus translation in the backend.
- The frontend now records audio and uploads it to the backend, Google Speech-to-Text converts it to text, Google Translate converts the text, and browser speech synthesis speaks the translated result.
- WebSocket endpoint: `/ws`
