import { describe, expect, it, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { GenerateKeyForm } from "./GenerateKeyForm";

describe("GenerateKeyForm", () => {
  it("submits just a name when no optional fields are filled in", () => {
    const onSubmit = vi.fn();
    render(<GenerateKeyForm onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText(/^name/i), { target: { value: "CI pipeline" } });
    fireEvent.click(screen.getByRole("button", { name: /generate key/i }));

    expect(onSubmit).toHaveBeenCalledWith({ name: "CI pipeline" });
  });

  it("does not submit without a name", () => {
    const onSubmit = vi.fn();
    render(<GenerateKeyForm onSubmit={onSubmit} />);
    fireEvent.click(screen.getByRole("button", { name: /generate key/i }));
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("includes scopes added via the TagListInput", () => {
    const onSubmit = vi.fn();
    render(<GenerateKeyForm onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText(/^name/i), { target: { value: "Partner integration" } });
    fireEvent.change(screen.getByPlaceholderText(/read:orders/i), { target: { value: "read:orders" } });
    // Two "Add" buttons exist (one for scopes, one for IPs); the first belongs to scopes.
    fireEvent.click(screen.getAllByRole("button", { name: /^add$/i })[0]);
    fireEvent.click(screen.getByRole("button", { name: /generate key/i }));

    expect(onSubmit).toHaveBeenCalledWith(
      expect.objectContaining({ name: "Partner integration", scopes: ["read:orders"] })
    );
  });

  it("includes a numeric rate limit when provided", () => {
    const onSubmit = vi.fn();
    render(<GenerateKeyForm onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText(/^name/i), { target: { value: "Rate limited key" } });
    fireEvent.change(screen.getByLabelText(/rate limit/i), { target: { value: "60" } });
    fireEvent.click(screen.getByRole("button", { name: /generate key/i }));

    expect(onSubmit).toHaveBeenCalledWith(
      expect.objectContaining({ name: "Rate limited key", rateLimitPerMinute: 60 })
    );
  });

  it("includes allowed IPs added via the TagListInput", () => {
    const onSubmit = vi.fn();
    render(<GenerateKeyForm onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText(/^name/i), { target: { value: "IP restricted key" } });
    fireEvent.change(screen.getByPlaceholderText(/10\.0\.0\.0/i), { target: { value: "10.0.0.0/8" } });
    // Two "Add" buttons exist; the second belongs to the IP allowlist.
    fireEvent.click(screen.getAllByRole("button", { name: /^add$/i })[1]);
    fireEvent.click(screen.getByRole("button", { name: /generate key/i }));

    expect(onSubmit).toHaveBeenCalledWith(
      expect.objectContaining({ name: "IP restricted key", allowedIps: ["10.0.0.0/8"] })
    );
  });

  it("clears the form after a successful submit", () => {
    const onSubmit = vi.fn();
    render(<GenerateKeyForm onSubmit={onSubmit} />);

    const nameInput = screen.getByLabelText(/^name/i) as HTMLInputElement;
    fireEvent.change(nameInput, { target: { value: "Temp" } });
    fireEvent.click(screen.getByRole("button", { name: /generate key/i }));

    expect(nameInput.value).toBe("");
  });
});
