import { useState, type KeyboardEvent } from "react";

interface TagListInputProps {
  label: string;
  placeholder: string;
  values: string[];
  onChange: (values: string[]) => void;
}

/** A small "type a value, press Enter or click Add" repeating-field input,
 * used for both scopes and IP allowlist entries -- free-form lists where a
 * fixed dropdown doesn't make sense. */
export function TagListInput({ label, placeholder, values, onChange }: TagListInputProps) {
  const [draft, setDraft] = useState("");

  function commit() {
    const trimmed = draft.trim();
    if (trimmed && !values.includes(trimmed)) {
      onChange([...values, trimmed]);
    }
    setDraft("");
  }

  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") {
      e.preventDefault();
      commit();
    }
  }

  function remove(value: string) {
    onChange(values.filter((v) => v !== value));
  }

  return (
    <div className="stack">
      <label>{label}</label>
      <div className="row">
        <input
          placeholder={placeholder}
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button type="button" className="secondary" onClick={commit}>
          Add
        </button>
      </div>
      {values.length > 0 && (
        <div className="row">
          {values.map((value) => (
            <span
              key={value}
              className="tag tag--removable"
              role="button"
              aria-label={`Remove ${value}`}
              onClick={() => remove(value)}
            >
              {value}
            </span>
          ))}
        </div>
      )}
    </div>
  );
}
