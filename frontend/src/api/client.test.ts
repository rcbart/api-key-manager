import { describe, expect, it, vi, beforeEach } from "vitest";
import { apiFetch, setAuthToken, setUnauthorizedHandler, ApiError } from "./client";

// jsdom supplies a stub `fetch`; we replace it per test via vi.stubGlobal.
function mockFetch(status: number, body: unknown) {
  const json = JSON.stringify(body);
  vi.stubGlobal(
    "fetch",
    vi.fn().mockResolvedValue({
      ok: status >= 200 && status < 300,
      status,
      text: () => Promise.resolve(json)
    })
  );
}

beforeEach(() => {
  // Reset module-level state between tests.
  setAuthToken(undefined);
  setUnauthorizedHandler(undefined);
  vi.unstubAllGlobals();
});

describe("apiFetch", () => {
  it("returns the parsed response body on 2xx", async () => {
    mockFetch(200, { id: "k1", name: "Test" });
    const result = await apiFetch<{ id: string; name: string }>("/api/admin/keys");
    expect(result).toEqual({ id: "k1", name: "Test" });
  });

  it("throws ApiError with the server message and code on 4xx", async () => {
    mockFetch(400, { message: "Invalid input", code: "VALIDATION_ERROR" });
    await expect(apiFetch("/api/admin/keys")).rejects.toMatchObject({
      status: 400,
      code: "VALIDATION_ERROR",
      message: "Invalid input"
    });
  });

  it("attaches Authorization header when authToken is set", async () => {
    setAuthToken("jwt-token-abc");
    mockFetch(200, {});
    await apiFetch("/api/admin/keys");

    const calls = (global.fetch as ReturnType<typeof vi.fn>).mock.calls;
    const headers = calls[0][1].headers as Record<string, string>;
    expect(headers["Authorization"]).toBe("Bearer jwt-token-abc");
  });

  it("omits Authorization header when no authToken is set", async () => {
    mockFetch(200, {});
    await apiFetch("/api/admin/keys");

    const calls = (global.fetch as ReturnType<typeof vi.fn>).mock.calls;
    const headers = calls[0][1].headers as Record<string, string>;
    expect(headers["Authorization"]).toBeUndefined();
  });

  it("calls the unauthorized handler on 401", async () => {
    const onUnauthorized = vi.fn();
    setUnauthorizedHandler(onUnauthorized);
    mockFetch(401, { message: "Unauthorized", code: "UNAUTHORIZED" });

    // apiFetch will throw since it's not ok, but handler should still fire
    await apiFetch("/api/admin/keys").catch(() => {});
    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

  it("does not call the unauthorized handler on other error codes", async () => {
    const onUnauthorized = vi.fn();
    setUnauthorizedHandler(onUnauthorized);
    mockFetch(403, { message: "Forbidden", code: "FORBIDDEN" });

    await apiFetch("/api/admin/keys").catch(() => {});
    expect(onUnauthorized).not.toHaveBeenCalled();
  });

  it("throws a NETWORK_ERROR ApiError when fetch rejects", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));
    await expect(apiFetch("/api/admin/keys")).rejects.toMatchObject({
      code: "NETWORK_ERROR",
      status: 0
    });
  });

  it("handles an empty response body (e.g. 204 No Content) without throwing", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({ ok: true, status: 204, text: () => Promise.resolve("") })
    );
    const result = await apiFetch("/api/admin/keys/k1");
    expect(result).toBeUndefined();
  });
});

describe("ApiError", () => {
  it("has the correct name, status, and code properties", () => {
    const err = new ApiError("Something went wrong", 422, "UNPROCESSABLE");
    expect(err.name).toBe("ApiError");
    expect(err.status).toBe(422);
    expect(err.code).toBe("UNPROCESSABLE");
    expect(err.message).toBe("Something went wrong");
  });
});
