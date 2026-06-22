const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

export class ApiError extends Error {
  status: number;
  code?: string;
  details?: unknown;

  constructor(message: string, status: number, code?: string, details?: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

// Module-level token, set by AuthContext on login/logout/restore. Kept out
// of React state so the plain api/* modules (which aren't components) can
// read it without a context dependency. See docs/SECURITY.md for why this
// lives in sessionStorage (not localStorage) and never in a JS-readable
// cookie.
let authToken: string | undefined;

export function setAuthToken(token: string | undefined) {
  authToken = token;
}

/** Called once when a 401 comes back, so the UI can drop the user back to the login screen. */
let onUnauthorized: (() => void) | undefined;

export function setUnauthorizedHandler(handler: (() => void) | undefined) {
  onUnauthorized = handler;
}

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const url = `${BASE_URL}${path}`;
  let res: Response;

  try {
    res = await fetch(url, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...(authToken ? { Authorization: `Bearer ${authToken}` } : {}),
        ...(init?.headers ?? {})
      }
    });
  } catch (networkErr) {
    console.error(`[api] network error calling ${init?.method ?? "GET"} ${url}`, networkErr);
    throw new ApiError("Could not reach the server. Is the backend running and reachable?", 0, "NETWORK_ERROR");
  }

  if (res.status === 401 && onUnauthorized) {
    onUnauthorized();
  }

  const text = await res.text();
  const json = text ? safeJsonParse(text) : undefined;

  if (!res.ok) {
    const message = json?.message ?? `Request failed with status ${res.status}`;
    const code = json?.code ?? "UNKNOWN_ERROR";
    console.error(`[api] ${init?.method ?? "GET"} ${url} -> ${res.status}`, json);
    throw new ApiError(message, res.status, code, json?.details);
  }

  return json as T;
}

function safeJsonParse(text: string): any {
  try {
    return JSON.parse(text);
  } catch {
    return undefined;
  }
}

export const api = {
  get: <T>(path: string) => apiFetch<T>(path),
  post: <T>(path: string, body?: unknown) => apiFetch<T>(path, { method: "POST", body: JSON.stringify(body ?? {}) }),
  patch: <T>(path: string, body?: unknown) => apiFetch<T>(path, { method: "PATCH", body: JSON.stringify(body ?? {}) }),
  delete: <T>(path: string) => apiFetch<T>(path, { method: "DELETE" })
};
