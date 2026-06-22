import { useState, type FormEvent } from "react";
import { useAuth } from "../auth/AuthContext";

export function LoginPage() {
  const { login, isLoggingIn, loginError } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    try {
      await login(username, password);
    } catch {
      // loginError is already set by AuthContext; nothing else to do here.
    }
  }

  return (
    <div className="login-shell">
      <form className="panel stack" onSubmit={handleSubmit} aria-label="Admin login">
        <h1>API Key Manager</h1>
        <p className="empty-state" style={{ textAlign: "left", padding: 0 }}>
          Sign in with an admin account to generate and manage API keys.
        </p>
        <label>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} autoFocus required />
        </label>
        <label>
          Password
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        {loginError && <p className="validation-issue">{loginError}</p>}
        <button type="submit" disabled={isLoggingIn}>
          {isLoggingIn ? "Signing in..." : "Sign in"}
        </button>
      </form>
    </div>
  );
}
