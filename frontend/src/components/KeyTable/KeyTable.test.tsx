import { describe, expect, it, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { KeyTable } from "./KeyTable";
import type { ApiKeySummary } from "../../types";

function makeKey(overrides: Partial<ApiKeySummary> = {}): ApiKeySummary {
  return {
    id: "key-1",
    name: "Test key",
    displayPrefix: "ak_live_AbCd",
    scopes: [],
    allowedIps: [],
    rateLimitPerMinute: null,
    expiresAt: null,
    revoked: false,
    revokedAt: null,
    createdBy: "admin",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    lastUsedAt: null,
    ...overrides,
  };
}

describe("KeyTable", () => {
  it("shows an empty state message when there are no keys", () => {
    render(<KeyTable keys={[]} onRevoke={vi.fn()} onDelete={vi.fn()} onUpdate={vi.fn()} />);
    expect(screen.getByText(/no api keys yet/i)).toBeTruthy();
  });

  it("renders one row per key with its name and display prefix", () => {
    const keys = [
      makeKey({ id: "k1", name: "Production", displayPrefix: "ak_live_Aa" }),
      makeKey({ id: "k2", name: "Staging", displayPrefix: "ak_live_Bb" }),
    ];
    render(<KeyTable keys={keys} onRevoke={vi.fn()} onDelete={vi.fn()} onUpdate={vi.fn()} />);

    expect(screen.getByText("Production")).toBeTruthy();
    expect(screen.getByText("Staging")).toBeTruthy();
    expect(screen.getByText("ak_live_Aa...")).toBeTruthy();
    expect(screen.getByText("ak_live_Bb...")).toBeTruthy();
  });

  it("shows Active status for a live, non-revoked key", () => {
    render(<KeyTable keys={[makeKey()]} onRevoke={vi.fn()} onDelete={vi.fn()} onUpdate={vi.fn()} />);
    expect(screen.getByText("Active")).toBeTruthy();
  });

  it("shows Revoked status for a revoked key", () => {
    render(
      <KeyTable
        keys={[makeKey({ revoked: true, revokedAt: new Date().toISOString() })]}
        onRevoke={vi.fn()}
        onDelete={vi.fn()}
        onUpdate={vi.fn()}
      />
    );
    expect(screen.getByText("Revoked")).toBeTruthy();
  });

  it("shows Expired status for a key whose expiresAt is in the past", () => {
    const pastIso = new Date(Date.now() - 86_400_000).toISOString(); // 1 day ago
    render(
      <KeyTable
        keys={[makeKey({ expiresAt: pastIso })]}
        onRevoke={vi.fn()}
        onDelete={vi.fn()}
        onUpdate={vi.fn()}
      />
    );
    expect(screen.getByText("Expired")).toBeTruthy();
  });

  it("calls onRevoke with the key id when Revoke is clicked", () => {
    const onRevoke = vi.fn();
    render(<KeyTable keys={[makeKey({ id: "k-abc" })]} onRevoke={onRevoke} onDelete={vi.fn()} onUpdate={vi.fn()} />);
    fireEvent.click(screen.getByRole("button", { name: /revoke/i }));
    expect(onRevoke).toHaveBeenCalledWith("k-abc");
  });

  it("calls onDelete with the key id when Delete is clicked", () => {
    const onDelete = vi.fn();
    render(<KeyTable keys={[makeKey({ id: "k-del" })]} onRevoke={vi.fn()} onDelete={onDelete} onUpdate={vi.fn()} />);
    fireEvent.click(screen.getByRole("button", { name: /delete/i }));
    expect(onDelete).toHaveBeenCalledWith("k-del");
  });

  it("hides Revoke and Edit buttons for a revoked key", () => {
    render(
      <KeyTable
        keys={[makeKey({ revoked: true, revokedAt: new Date().toISOString() })]}
        onRevoke={vi.fn()}
        onDelete={vi.fn()}
        onUpdate={vi.fn()}
      />
    );
    expect(screen.queryByRole("button", { name: /revoke/i })).toBeNull();
    expect(screen.queryByRole("button", { name: /edit/i })).toBeNull();
    // Delete should still be present
    expect(screen.getByRole("button", { name: /delete/i })).toBeTruthy();
  });

  it("toggles the EditKeyForm when Edit is clicked and hides it on Cancel", () => {
    render(<KeyTable keys={[makeKey()]} onRevoke={vi.fn()} onDelete={vi.fn()} onUpdate={vi.fn()} />);

    // Edit form not visible initially
    expect(screen.queryByRole("button", { name: /save changes/i })).toBeNull();

    // Click Edit to open the form
    fireEvent.click(screen.getByRole("button", { name: /^edit$/i }));
    expect(screen.getByRole("button", { name: /save changes/i })).toBeTruthy();

    // Click Cancel to close it — two "Cancel" buttons exist (row toggle + form); either works
    fireEvent.click(screen.getAllByRole("button", { name: /^cancel$/i })[0]);
    expect(screen.queryByRole("button", { name: /save changes/i })).toBeNull();
  });

  it("calls onUpdate and closes the edit form when Save changes is submitted", () => {
    const onUpdate = vi.fn();
    render(<KeyTable keys={[makeKey({ id: "ku-1", name: "Old name" })]} onRevoke={vi.fn()} onDelete={vi.fn()} onUpdate={onUpdate} />);

    fireEvent.click(screen.getByRole("button", { name: /^edit$/i }));

    // Change the name in the edit form
    const nameInput = screen.getByDisplayValue("Old name") as HTMLInputElement;
    fireEvent.change(nameInput, { target: { value: "New name" } });

    fireEvent.click(screen.getByRole("button", { name: /save changes/i }));

    expect(onUpdate).toHaveBeenCalledWith("ku-1", expect.objectContaining({ name: "New name" }));
    // Edit form should close
    expect(screen.queryByRole("button", { name: /save changes/i })).toBeNull();
  });
});
