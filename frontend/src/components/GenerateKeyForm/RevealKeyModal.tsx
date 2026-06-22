import { useState } from "react";
import { useToast } from "../common/ToastContext";

interface RevealKeyModalProps {
  rawKey: string;
  keyName: string;
  onClose: () => void;
}

/** Shown exactly once, right after a key is created. There is no "view key" anywhere else in the app -- this is the only chance to copy it. */
export function RevealKeyModal({ rawKey, keyName, onClose }: RevealKeyModalProps) {
  const [acknowledged, setAcknowledged] = useState(false);
  const [copiedSample, setCopiedSample] = useState<"key" | "curl" | "http" | null>(null);
  const { showToast } = useToast();

  const validateUrl = `${window.location.origin}/api/validate`;

  const curlSample =
    `curl -X POST ${validateUrl} \\\n` +
    `  -H "X-API-Key: ${rawKey}" \\\n` +
    `  -H "Content-Type: application/json" \\\n` +
    `  -d '{"requiredScope": "your:scope"}'`;

  const httpSample =
    `POST /api/validate HTTP/1.1\n` +
    `Host: ${window.location.host}\n` +
    `X-API-Key: ${rawKey}\n` +
    `Content-Type: application/json\n\n` +
    `{"requiredScope": "your:scope"}`;

  async function copyText(text: string, which: "key" | "curl" | "http") {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedSample(which);
      setTimeout(() => setCopiedSample(null), 2000);
      showToast("Copied to clipboard", "success");
    } catch {
      showToast("Couldn't copy automatically — select and copy manually.", "error");
    }
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true" aria-label="New API key">
      <div className="modal panel" style={{ width: 580, maxHeight: "90vh", overflowY: "auto" }}>
        <h2>Key created: {keyName}</h2>
        <p className="error-panel__message">
          This is the only time this key will be shown. Copy it now and store it somewhere safe — if you lose it,
          revoke it and generate a new one.
        </p>

        {/* Raw key */}
        <p style={{ fontSize: 12, color: "var(--color-text-dim)", margin: "12px 0 4px" }}>Your API key</p>
        <pre className="json-view" data-testid="raw-key" style={{ marginBottom: 4 }}>
          {rawKey}
        </pre>
        <div className="row" style={{ marginBottom: 16 }}>
          <button onClick={() => copyText(rawKey, "key")}>
            {copiedSample === "key" ? "Copied!" : "Copy key"}
          </button>
        </div>

        {/* Usage examples */}
        <p style={{ fontSize: 13, fontWeight: 600, margin: "0 0 8px" }}>How to use this key</p>
        <p style={{ fontSize: 12, color: "var(--color-text-dim)", margin: "0 0 6px" }}>
          Send the key in the <code style={{ background: "var(--color-panel-alt)", padding: "1px 4px", borderRadius: 3 }}>X-API-Key</code> header
          when calling <code style={{ background: "var(--color-panel-alt)", padding: "1px 4px", borderRadius: 3 }}>POST /api/validate</code> from your service.
        </p>

        <p style={{ fontSize: 12, color: "var(--color-text-dim)", margin: "10px 0 4px" }}>curl example</p>
        <pre className="json-view" style={{ marginBottom: 4, fontSize: 11 }}>{curlSample}</pre>
        <div className="row" style={{ marginBottom: 12 }}>
          <button className="secondary" style={{ fontSize: 11 }} onClick={() => copyText(curlSample, "curl")}>
            {copiedSample === "curl" ? "Copied!" : "Copy curl"}
          </button>
        </div>

        <p style={{ fontSize: 12, color: "var(--color-text-dim)", margin: "0 0 4px" }}>HTTP request</p>
        <pre className="json-view" style={{ marginBottom: 4, fontSize: 11 }}>{httpSample}</pre>
        <div className="row" style={{ marginBottom: 16 }}>
          <button className="secondary" style={{ fontSize: 11 }} onClick={() => copyText(httpSample, "http")}>
            {copiedSample === "http" ? "Copied!" : "Copy HTTP"}
          </button>
        </div>

        <p style={{ fontSize: 12, color: "var(--color-text-dim)", margin: "0 0 12px" }}>
          The response is always HTTP 200. Check the <code style={{ background: "var(--color-panel-alt)", padding: "1px 4px", borderRadius: 3 }}>valid</code> field —
          {" "}<code style={{ background: "var(--color-panel-alt)", padding: "1px 4px", borderRadius: 3 }}>true</code> means the key passed all checks,
          {" "}<code style={{ background: "var(--color-panel-alt)", padding: "1px 4px", borderRadius: 3 }}>false</code> includes a <code style={{ background: "var(--color-panel-alt)", padding: "1px 4px", borderRadius: 3 }}>reason</code> field.
        </p>

        <label className="row" style={{ marginBottom: 12 }}>
          <input type="checkbox" checked={acknowledged} onChange={(e) => setAcknowledged(e.target.checked)} />
          I've copied this key and understand it won't be shown again
        </label>
        <button className="secondary" disabled={!acknowledged} onClick={onClose}>
          Done
        </button>
      </div>
    </div>
  );
}
