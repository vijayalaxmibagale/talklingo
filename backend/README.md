# TalkLingo Live Backend

TalkLingo Live is a real-time multilingual communication platform that helps people converse seamlessly across language barriers using live voice translation. The system captures speech input, converts it to text, translates it into the target language, and delivers the result as both text and speech with minimal delay.

This repository contains the Java Spring Boot backend that powers session management, translation orchestration, real-time delivery, and Oracle-ready persistence for the broader TalkLingo Live platform, which pairs with a React frontend for a responsive user experience.

## Platform overview

TalkLingo Live is designed to reduce communication friction in:

- Global team collaboration
- Multilingual customer support
- Remote meetings and cross-border interactions

At a high level, the end-to-end platform combines:

- Java Spring Boot for backend APIs and real-time messaging
- React for the user-facing web experience
- Oracle Database for scalable persistence
- A translation pipeline that supports speech-to-text and translation workflows

## What is included

- User registration and login endpoints with JWT generation
- Conversation session management
- Message translation pipeline with a mock provider or Google Cloud Translate
- Voice message upload endpoint with Google Cloud Speech-to-Text support
- Generated audio URL placeholders for text-to-speech output
- WebSocket topic broadcasting for real-time message delivery
- Oracle-ready JPA entities and repositories

## API endpoints

### Register

`POST /api/users/register`

```json
{
  "name": "Asha",
  "email": "asha@example.com",
  "password": "secret123"
}
```

### Login

`POST /api/users/login`

```json
{
  "email": "asha@example.com",
  "password": "secret123"
}
```

### Create session

`POST /api/sessions`

```json
{
  "title": "Support Call - English to Spanish",
  "sourceLanguage": "en",
  "targetLanguage": "es"
}
```

### Send translated message

`POST /api/sessions/{sessionId}/messages`

```json
{
  "senderName": "Asha",
  "sourceLanguage": "en",
  "targetLanguage": "es",
  "text": "Hello, how can I help you today?"
}
```

### Send recorded voice message

`POST /api/sessions/{sessionId}/voice-messages`

Multipart form data:

- `senderName`
- `sourceLanguage`
- `targetLanguage`
- `audio`

### View session details

`GET /api/sessions/{sessionId}`

## WebSocket

- Endpoint: `/ws`
- Topic subscription: `/topic/sessions/{sessionId}`

Each new translated message is published to the session topic after it is stored.

## Frontend integration notes

- Set `talklingo.translation.provider=google` and provide `GOOGLE_TRANSLATE_API_KEY` to enable real text translation through Google Cloud Translate.
- Set `talklingo.speech.provider=google` to enable Google Cloud Speech-to-Text for uploaded voice messages.
- Browser voice playback is still handled in the React frontend; the backend now performs speech-to-text and translated text delivery.
- The current default provider is still `mock`, so local development works without external AI services.
- The `audioUrl` field is currently a generated placeholder URL that represents where synthesized speech would be served from.

## Translation provider configuration

### Mock provider

Default:

```properties
talklingo.translation.provider=mock
```

### Google Cloud Translate provider

Use these environment variables before starting the backend:

```powershell
$env:TALKLINGO_TRANSLATION_PROVIDER="google"
$env:TALKLINGO_SPEECH_PROVIDER="google"
$env:GOOGLE_TRANSLATE_API_KEY="your-google-api-key"
```

Or set the equivalent application properties:

```properties
talklingo.translation.provider=google
talklingo.speech.provider=google
talklingo.translation.google.api-key=your-google-api-key
```

When Google speech and translation are enabled, recorded voice or typed text sent as `en -> hi` or `hi -> en` will be translated into the target language before they are broadcast to the session.

## Suggested next steps

1. Build the React UI for session creation, live transcript view, and audio playback.
2. Add STT and TTS streaming integrations.
3. Protect session endpoints with JWT authentication once the frontend login flow is in place.
4. Add participant tracking and language preference switching per user.
