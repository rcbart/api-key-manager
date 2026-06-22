import { useState, type FormEvent } from "react";
import { TagListInput } from "../GenerateKeyForm/TagListInput";
import type { ApiKeySummary, UpdateApiKeyInput } from "../../types";

interface EditKeyFormProps {
  apiKey: ApiKeySummary;
  onSubmit: (input: UpdateApiKeyInput) => void;
  onCancel: () => void;
  submitting?: boolean;
}

/** Edits everything about a key except its secret value -- the raw key
 * itself can never be changed or re-shown after creation. */
export function EditKeyForm({ apiKey, onSubmit, onCancel, submitting }: EditKeyFormProps) {
  const [name, setName] = useState(apiKey.name);
  const [scopes, setScopes] = useState<string[]>(apiKey.scopes);
  const [rateLimitPerMinute, setRateLimitPerMinute] = useState(
    apiKey.rateLimitPerMinute != null ? String(apiKey.rateLimitPerMinute) : ""
  );
  const [allowedIps, setAllowedIps] = useState<string[]>(apiKey.allowedIps);

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const input: UpdateApiKeyInput = {
      name: name.trim() || apiKey.name,
      scopes,
      allowedIps
    };
    if (rateLimitPerMinute.trim()) {
      input.rateLimitPerMinute = Number(rateLimitPerMinute);
    } else {
      input.clearRateLimit = true;
    }
    onSubmit(input);
  }

  return (
    <form className="stack panel" onSubmit={handleSubmit} aria-label={`Edit ${apiKey.name}`}>
      <label>
        Name
        <input value={name} onChange={(e) => setName(e.target.value)} />
      </label>

      <TagListInput label="Scopes" placeholder="e.g. read:orders" values={scopes} onChange={setScopes} />

      <label>
        Rate limit, requests per minute (blank = unlimited)
        <input
          type="number"
          min={1}
          value={rateLimitPerMinute}
          onChange={(e) => setRateLimitPerMinute(e.target.value)}
        />
      </label>

      <TagListInput
        label="Allowed IPs / CIDR blocks"
        placeholder="e.g. 10.0.0.0/8"
        values={allowedIps}
        onChange={setAllowedIps}
      />

      <div className="row">
        <button type="submit" disabled={submitting}>
          Save changes
        </button>
        <button type="button" className="secondary" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
}
