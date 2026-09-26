# DocPilot Full Stack

This project contains:

- `backend` - Spring Boot + GitHub OAuth + repository analysis + Gemini documentation generation
- `frontend` - React + Vite UI

## Architecture

React -> GitHub OAuth -> Spring Boot -> GitHub REST API -> repository tree/blob analysis -> Gemini -> Markdown documentation

## GitHub OAuth setup

Create a GitHub OAuth App and configure:

- Homepage URL: `http://localhost:5173`
- Authorization callback URL: `http://localhost:8080/api/github/callback`

Set these environment variables before starting the backend:

```powershell
setx GITHUB_CLIENT_ID "..."
setx GITHUB_CLIENT_SECRET "..."
setx GITHUB_REDIRECT_URI "http://localhost:8080/api/github/callback"
setx GITHUB_FRONTEND_URL "http://localhost:5173"
setx GEMINI_API_KEY "..."
setx GEMINI_MODEL "gemini-3.8-flash"
```

Restart IntelliJ/terminal after `setx`.

## Start backend

```bash
cd backend
mvn spring-boot:run
```

## Start frontend

In another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## Important

This is a local-development MVP. It keeps the GitHub access token in the server-side HTTP session. For production, use a persistent server-side session/token store, HTTPS, stronger exception handling, repository-size job processing, and a database if you need long-lived accounts.
