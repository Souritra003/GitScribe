import { useEffect, useState } from "react";
import {
  analyzeRepository,
  getCurrentUser,
  getRepositories,
  loginWithGitHub,
  logout
} from "./api";

function App() {
  const [user, setUser] = useState(null);
  const [repositories, setRepositories] = useState([]);
  const [selectedRepo, setSelectedRepo] = useState(null);
  const [documentation, setDocumentation] = useState("");
  const [loadingRepos, setLoadingRepos] = useState(false);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    checkConnection();

    const params = new URLSearchParams(window.location.search);
    if (params.get("error")) {
      setError("GitHub authorization was not completed.");
      window.history.replaceState({}, "", "/");
    }

    if (params.get("connected")) {
      window.history.replaceState({}, "", "/");
    }
  }, []);

  async function checkConnection() {
    try {
      const currentUser = await getCurrentUser();
      setUser(currentUser);

      if (currentUser.connected) {
        loadRepositories();
      }
    } catch (e) {
      setError(e.message);
    }
  }

  async function loadRepositories() {
    setLoadingRepos(true);
    setError("");

    try {
      const data = await getRepositories();
      setRepositories(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoadingRepos(false);
    }
  }

  async function handleAnalyze(repo) {
    setSelectedRepo(repo);
    setDocumentation("");
    setAnalyzing(true);
    setError("");

    try {
      const result = await analyzeRepository(
        repo.fullName.split("/")[0],
        repo.name
      );

      setDocumentation(result.documentation);
    } catch (e) {
      setError(e.message);
    } finally {
      setAnalyzing(false);
    }
  }

  async function handleLogout() {
    await logout();
    setUser(null);
    setRepositories([]);
    setSelectedRepo(null);
    setDocumentation("");
  }

  return (
    <div className="app">
      <header className="navbar">
        <div className="brand">
          <div className="brand-icon">D</div>
          <span>DocPilot</span>
        </div>

        {user?.connected && (
          <div className="user-area">
            {user.avatarUrl && (
              <img src={user.avatarUrl} alt="" className="avatar" />
            )}
            <span>{user.name || user.login}</span>
            <button className="secondary-button" onClick={handleLogout}>
              Disconnect
            </button>
          </div>
        )}
      </header>

      <main>
        {!user?.connected ? (
          <section className="hero">
            <div className="hero-badge">AI CODEBASE DOCUMENTATION</div>
            <h1>
              Understand your
              <span> GitHub repositories.</span>
            </h1>

            <p>
              Connect GitHub, select a repository, and let DocPilot analyze
              its structure, technologies, APIs, components and source code.
            </p>

            <button className="github-button" onClick={loginWithGitHub}>
              <span className="github-mark">●</span>
              Connect GitHub
            </button>

            <div className="flow">
              <div>Connect</div>
              <span>→</span>
              <div>Select</div>
              <span>→</span>
              <div>Analyze</div>
              <span>→</span>
              <div>Document</div>
            </div>
          </section>
        ) : (
          <section className="dashboard">
            <div className="dashboard-heading">
              <div>
                <div className="hero-badge">GITHUB CONNECTED</div>
                <h1>Your repositories</h1>
                <p>Select a repository to generate documentation.</p>
              </div>

              <button
                className="secondary-button"
                onClick={loadRepositories}
              >
                Refresh
              </button>
            </div>

            {loadingRepos ? (
              <div className="state-card">Loading repositories...</div>
            ) : repositories.length === 0 ? (
              <div className="state-card">
                No repositories were returned by GitHub.
              </div>
            ) : (
              <div className="repo-grid">
                {repositories.map((repo) => (
                  <article className="repo-card" key={repo.fullName}>
                    <div className="repo-top">
                      <div className="repo-icon">⌘</div>
                      {repo.privateRepository && (
                        <span className="private-badge">Private</span>
                      )}
                    </div>

                    <h3>{repo.name}</h3>
                    <p>{repo.description || "No description available."}</p>

                    <div className="repo-meta">
                      <span>{repo.defaultBranch}</span>
                    </div>

                    <button
                      className="analyze-button"
                      onClick={() => handleAnalyze(repo)}
                      disabled={analyzing}
                    >
                      {selectedRepo?.fullName === repo.fullName && analyzing
                        ? "Analyzing..."
                        : "Analyze repository"}
                    </button>
                  </article>
                ))}
              </div>
            )}

            {error && <div className="error">{error}</div>}

            {documentation && (
              <section className="documentation-panel">
                <div className="documentation-header">
                  <div>
                    <div className="hero-badge">ANALYSIS COMPLETE</div>
                    <h2>{selectedRepo?.name}</h2>
                  </div>

                  <button
                    className="secondary-button"
                    onClick={() => navigator.clipboard.writeText(documentation)}
                  >
                    Copy
                  </button>
                </div>

                <pre>{documentation}</pre>
              </section>
            )}
          </section>
        )}

        {error && !user?.connected && <div className="error global-error">{error}</div>}
      </main>
    </div>
  );
}

export default App;
