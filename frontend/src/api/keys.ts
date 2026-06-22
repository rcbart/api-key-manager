import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "./client";
import type { ApiKeySummary, CreateApiKeyInput, CreateApiKeyResult, UpdateApiKeyInput } from "../types";

const KEYS_QUERY_KEY = ["api-keys"];

export function useApiKeys() {
  return useQuery({
    queryKey: KEYS_QUERY_KEY,
    queryFn: () => api.get<ApiKeySummary[]>("/api/admin/keys")
  });
}

export function useCreateApiKey() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateApiKeyInput) => api.post<CreateApiKeyResult>("/api/admin/keys", input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: KEYS_QUERY_KEY })
  });
}

export function useUpdateApiKey() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: UpdateApiKeyInput }) =>
      api.patch<ApiKeySummary>(`/api/admin/keys/${id}`, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: KEYS_QUERY_KEY })
  });
}

export function useRevokeApiKey() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.post<ApiKeySummary>(`/api/admin/keys/${id}/revoke`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: KEYS_QUERY_KEY })
  });
}

export function useDeleteApiKey() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.delete(`/api/admin/keys/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: KEYS_QUERY_KEY })
  });
}
