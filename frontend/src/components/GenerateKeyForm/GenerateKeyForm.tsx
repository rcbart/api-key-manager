import { useState, type FormEvent } from "react";
import { TagListInput } from "./TagListInput";
import type { CreateApiKeyInput } from "../../types";

interface GenerateKeyFormProps {
  onSubmit: (input: CreateApiKeyInput) => void;
  submitting?: boolean;
}

/**
 * Builds a CreateApiKeyInput from the customizable generation parameters:
 * name (required), and optionally an expiration date, scopes, a per-minute
 * rate limit, and an IP/CIDR allowlist. Pure form-state logic, no API
 * calls -- the caller decides what to do with the assembled input.
 */
export function GenerateKeyForm({ onSubmit, submitting }: GenerateKeyFormProps) {
  const [name, setName] = useState("");
  const [expiresAt, setExpiresAt] = useState("");
  const [scopes, setScopes] = useState<string[]>([]);
  const [rateLimitPerMinute, setRateLimitPerMinute] = useState("");
  const [allowedIps, setAllowedIps] = useState<string[]>([]);

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!name.trim()) return;

    const input: CreateApiKeyInput = { name: name.trim() };
    if (expiresAt) {
      // <input type="datetime-local"> has no timezone; treat it as local
      // time and convert to an ISO instant for the API.
      input.expiresAt = new Date(expiresAt).toISOString();
    }
    if (scopes.length > 0) input.scopes = scopes;
    if (rateLimitPerMinute.trim()) input.rateLimitPerMinute = Number(rateLimitPerMinute);
    if (allowedIps.length > 0) input.allowedIps = allowedIps;

    onSubmit(input);

    setName("");
    setExpiresAt("");
    setScopes([]);
    setRateLimitPerMinute("");
    setAllowedIps([]);
  }

  return (
    <form className="stack" onSubmit={handleSubmit} aria-label="Generate API key">
      <label>
        Name
        <input
          placeholder="e.g. CI pipeline, Partner integration"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
      </label>

      <label>
        Expiration (optional -- leave blank for a key that never expires)
        <input type="datetime-local" value={expiresAt} onChange={(e) => setExpiresAt(e.target.value)} />
      </label>

      <TagListInput
        label="Scopes (optional -- leave empty for no scope restriction)"
        placeholder="e.g. read:orders"
        values={scopes}
        onChange={setScopes}
      />

      <label>
        Rate limit, requests per minute (optional -- leave blank for unlimited)
        <input
          type="number"
          min={1}
          placeholder="e.g. 60"
          value={rateLimitPerMinute}
          onChange={(e) => setRateLimitPerMinute(e.target.value)}
        />
      </label>

      <TagListInput
        label="Allowed IPs / CIDR blocks (optional -- leave empty to allow any source)"
        placeholder="e.g. 203.0.113.5 or 10.0.0.0/8"
        values={allowedIps}
        onChange={setAllowedIps}
      />

      <button type="submit" disabled={submitting || !name.trim()}>
        {submitting ? "Generating..." : "Generate key"}
      </button>
    </form>
  );
}
