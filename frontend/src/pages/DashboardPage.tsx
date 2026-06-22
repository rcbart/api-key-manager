import { useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { useApiKeys, useCreateApiKey, useDeleteApiKey, useRevokeApiKey, useUpdateApiKey } from "../api/keys";
import { useApiErrorToast } from "../hooks/useApiErrorToast";
import { useToast } from "../components/common/ToastContext";
import { GenerateKeyForm } from "../components/GenerateKeyForm/GenerateKeyForm";
import { RevealKeyModal } from "../components/GenerateKeyForm/RevealKeyModal";
import { KeyTable } from "../components/KeyTable/KeyTable";
import type { CreateApiKeyResult } from "../types";

export function DashboardPage() {
  const { username, logout } = useAuth();
  const { data: keys, isLoading, error } = useApiKeys();
  const createKey = useCreateApiKey();
  const updateKey = useUpdateApiKey();
  const revokeKey = useRevokeApiKey();
  const deleteKey = useDeleteApiKey();
  const { showToast } = useToast();

  const [revealed, setRevealed] = useState<CreateApiKeyResult | null>(null);

  useApiErrorToast(error);
  useApiErrorToast(createKey.error);
  useApiErrorToast(updateKey.error);
  useApiErrorToast(revokeKey.error);
  useApiErrorToast(deleteKey.error);

  return (
    <div className="stack">
      <div className="row" style={{ justifyContent: "space-between" }}>
        <h1>API Key Manager</h1>
        <div className="row">
          <span className="empty-state" style={{ padding: 0 }}>
            Signed in as {username}
          </span>
          <button className="secondary" onClick={logout}>
            Sign out
          </button>
        </div>
      </div>

      <div className="panel">
        <h2>Generate a new key</h2>
        <GenerateKeyForm
          submitting={createKey.isPending}
          onSubmit={(input) =>
            createKey.mutate(input, {
              onSuccess: (result) => setRevealed(result)
            })
          }
        />
      </div>

      <div className="panel">
        <h2>Keys</h2>
        {isLoading && <p>Loading...</p>}
        {keys && (
          <KeyTable
            keys={keys}
            updating={updateKey.isPending}
            onUpdate={(id, input) =>
              updateKey.mutate(
                { id, input },
                { onSuccess: () => showToast("Key updated", "success") }
              )
            }
            onRevoke={(id) =>
              revokeKey.mutate(id, { onSuccess: () => showToast("Key revoked", "success") })
            }
            onDelete={(id) => {
              if (window.confirm("Permanently delete this key? This cannot be undone.")) {
                deleteKey.mutate(id, { onSuccess: () => showToast("Key deleted", "success") });
              }
            }}
          />
        )}
      </div>

      {revealed && (
        <RevealKeyModal rawKey={revealed.rawKey} keyName={revealed.key.name} onClose={() => setRevealed(null)} />
      )}
    </div>
  );
}
