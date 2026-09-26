const API_BASE = "http://localhost:8080/api";

export async function getCurrentUser() {
  const response = await fetch(`${API_BASE}/github/me`, {
    credentials: "include"
  });

  if (!response.ok) {
    throw new Error("Unable to check GitHub connection.");
  }

  return response.json();
}

export async function getRepositories() {
  const response = await fetch(`${API_BASE}/github/repositories`, {
    credentials: "include"
  });

  if (response.status === 401) {
    throw new Error("NOT_CONNECTED");
  }

  if (!response.ok) {
    throw new Error("Unable to load repositories.");
  }

  return response.json();
}

export async function analyzeRepository(owner, repo) {
  const response = await fetch(`${API_BASE}/docpilot/analyze`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ owner, repo })
  });

  if (response.status === 401) {
    throw new Error("NOT_CONNECTED");
  }

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "Repository analysis failed.");
  }

  return response.json();
}

export async function logout() {
  await fetch(`${API_BASE}/github/logout`, {
    method: "POST",
    credentials: "include"
  });
}

export function loginWithGitHub() {
  window.location.href = `${API_BASE}/github/login`;
}
