import { Fragment, useState } from "react";
import { EditKeyForm } from "./EditKeyForm";
import type { ApiKeySummary, UpdateApiKeyInput } from "../../types";

interface KeyTableProps {
  keys: ApiKeySummary[];
  onRevoke: (id: string) => void;
  onDelete: (id: string) => void;
  onUpdate: (id: string, input: UpdateApiKeyInput) => void;
  updating?: boolean;
}

function statusOf(key: ApiKeySummary): { label: string; className: string } {
  if (key.revoked) return { label: "Revoked", className: "tag--revoked" };
  if (key.expiresAt && new Date(key.expiresAt).getTime() < Date.now()) {
    return { label: "Expired", className: "tag--revoked" };
  }
  return { label: "Active", className: "tag--active" };
}

export function KeyTable({ keys, onRevoke, onDelete, onUpdate, updating }: KeyTableProps) {
  const [editingId, setEditingId] = useState<string | null>(null);

  if (keys.length === 0) {
    return <p className="empty-state">No API keys yet -- generate one above.</p>;
  }

  return (
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Key</th>
          <th>Status</th>
          <th>Scopes</th>
          <th>Rate limit</th>
          <th>Expires</th>
          <th>Last used</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {keys.map((key) => {
          const status = statusOf(key);
          return (
            <Fragment key={key.id}>
              <tr>
                <td>{key.name}</td>
                <td>
                  <code>{key.displayPrefix}...</code>
                </td>
                <td>
                  <span className={`tag ${status.className}`}>{status.label}</span>
                </td>
                <td>{key.scopes.length > 0 ? key.scopes.join(", ") : <span className="empty-state">any</span>}</td>
                <td>{key.rateLimitPerMinute ?? "unlimited"}</td>
                <td>{key.expiresAt ? new Date(key.expiresAt).toLocaleString() : "never"}</td>
                <td>{key.lastUsedAt ? new Date(key.lastUsedAt).toLocaleString() : "never"}</td>
                <td className="row">
                  {!key.revoked && (
                    <>
                      <button className="secondary" onClick={() => setEditingId(editingId === key.id ? null : key.id)}>
                        {editingId === key.id ? "Cancel" : "Edit"}
                      </button>
                      <button className="danger" onClick={() => onRevoke(key.id)}>
                        Revoke
                      </button>
                    </>
                  )}
                  <button className="danger" onClick={() => onDelete(key.id)}>
                    Delete
                  </button>
                </td>
              </tr>
              {editingId === key.id && (
                <tr>
                  <td colSpan={8}>
                    <EditKeyForm
                      apiKey={key}
                      submitting={updating}
                      onCancel={() => setEditingId(null)}
                      onSubmit={(input) => {
                        onUpdate(key.id, input);
                        setEditingId(null);
                      }}
                    />
                  </td>
                </tr>
              )}
            </Fragment>
          );
        })}
      </tbody>
    </table>
  );
}
