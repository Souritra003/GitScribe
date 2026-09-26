# DocPilot Backend

Spring Boot backend for DocPilot.

## Requirements

- Java 21+
- Maven 3.6.3+
- GitHub OAuth App
- Gemini API key

## Environment variables

Windows PowerShell:

```powershell
setx GITHUB_CLIENT_ID "your_client_id"
setx GITHUB_CLIENT_SECRET "your_client_secret"
setx GITHUB_REDIRECT_URI "http://localhost:8080/api/github/callback"
setx GITHUB_FRONTEND_URL "http://localhost:5173"
setx GEMINI_API_KEY "your_gemini_key"
setx GEMINI_MODEL "gemini-3.8-flash"
```

Restart IntelliJ after using `setx`.

## GitHub OAuth App

Set the callback URL to:

`http://localhost:8080/api/github/callback`

The application uses OAuth state + PKCE and keeps the GitHub access token in the server-side HTTP session.

## Run

```bash
mvn spring-boot:run
```
