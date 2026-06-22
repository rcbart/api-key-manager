export interface ApiKeySummary {
  id: string;
  name: string;
  displayPrefix: string;
  scopes: string[];
  allowedIps: string[];
  rateLimitPerMinute: number | null;
  expiresAt: string | null;
  revoked: boolean;
  revokedAt: string | null;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  lastUsedAt: string | null;
}

export interface CreateApiKeyResult {
  rawKey: string;
  key: ApiKeySummary;
}

export interface CreateApiKeyInput {
  name: string;
  expiresAt?: string;
  scopes?: string[];
  rateLimitPerMinute?: number;
  allowedIps?: string[];
}

export interface UpdateApiKeyInput {
  name?: string;
  expiresAt?: string;
  clearExpiresAt?: boolean;
  scopes?: string[];
  rateLimitPerMinute?: number;
  clearRateLimit?: boolean;
  allowedIps?: string[];
}
