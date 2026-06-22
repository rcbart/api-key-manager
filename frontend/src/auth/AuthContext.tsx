import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { api, ApiError, setAuthToken, setUnauthorizedHandler } from "../api/client";

const STORAGE_KEY = "akm_session";

interface StoredSession {
  token: string;
  username: string;
}

interface LoginResponse {
  token: string;
  expiresAt: string;
}

interface AuthContextValue {
  username: string | undefined;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  loginError: string | undefined;
  isLoggingIn: boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readStoredSession(): StoredSession | undefined {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : undefined;
  } catch {
    return undefined;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [username, setUsername] = useState<string | undefined>(() => readStoredSession()?.username);
  const [loginError, setLoginError] = useState<string | undefined>();
  const [isLoggingIn, setIsLoggingIn] = useState(false);

  // Restore the token into the API client on first mount (sessionStorage
  // survives a page refresh within the same tab, but not a new tab/window --
  // see docs/SECURITY.md for why this tradeoff was chosen over localStorage).
  useEffect(() => {
    const stored = readStoredSession();
    if (stored) {
      setAuthToken(stored.token);
    }
  }, []);

  const logout = useCallback(() => {
    sessionStorage.removeItem(STORAGE_KEY);
    setAuthToken(undefined);
    setUsername(undefined);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(undefined);
  }, [logout]);

  const login = useCallback(async (loginUsername: string, password: string) => {
    setIsLoggingIn(true);
    setLoginError(undefined);
    try {
      const response = await api.post<LoginResponse>("/api/admin/auth/login", {
        username: loginUsername,
        password
      });
      setAuthToken(response.token);
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ token: response.token, username: loginUsername }));
      setUsername(loginUsername);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Login failed. Please try again.";
      setLoginError(message);
      throw err;
    } finally {
      setIsLoggingIn(false);
    }
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ username, isAuthenticated: !!username, login, logout, loginError, isLoggingIn }),
    [username, login, logout, loginError, isLoggingIn]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
