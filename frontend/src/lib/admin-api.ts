import { apiFetch } from '@/lib/api';
import type {
  AdminAccountRow,
  AdminAuditRow,
  AdminCardRow,
  AdminDashboardStats,
  AdminTransactionRow,
  AdminTransactionStats,
  AdminUserRow,
  SpringPage,
} from '@/lib/admin-types';

function jsonInit(body: unknown, init?: RequestInit): RequestInit {
  return {
    ...init,
    method: init?.method ?? 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers as Record<string, string>),
    },
    body: JSON.stringify(body),
  };
}

export function normalizePage<T>(page: SpringPage<T>): { items: T[]; total: number; page: number; pageSize: number } {
  return {
    items: page.content ?? [],
    total: page.totalElements ?? 0,
    page: (page.number ?? 0) + 1,
    pageSize: page.size ?? 20,
  };
}

export async function fetchAdminDashboardStats(token: string | null): Promise<AdminDashboardStats> {
  return apiFetch<AdminDashboardStats>('/api/admin/dashboard/stats', token);
}

export async function fetchAdminTransactionStats(token: string | null): Promise<AdminTransactionStats> {
  return apiFetch<AdminTransactionStats>('/api/admin/transactions/stats', token);
}

export async function fetchAdminUsers(token: string | null, page = 0, size = 50): Promise<SpringPage<AdminUserRow>> {
  return apiFetch<SpringPage<AdminUserRow>>(`/api/admin/users?page=${page}&size=${size}`, token);
}

export async function fetchAdminAccounts(
  token: string | null,
  page = 0,
  size = 50
): Promise<SpringPage<AdminAccountRow>> {
  return apiFetch<SpringPage<AdminAccountRow>>(`/api/admin/accounts?page=${page}&size=${size}`, token);
}

export async function fetchAdminCards(token: string | null, page = 0, size = 50): Promise<SpringPage<AdminCardRow>> {
  return apiFetch<SpringPage<AdminCardRow>>(`/api/admin/cards?page=${page}&size=${size}`, token);
}

export async function fetchAdminTransactions(
  token: string | null,
  page = 0,
  size = 50
): Promise<SpringPage<AdminTransactionRow>> {
  return apiFetch<SpringPage<AdminTransactionRow>>(`/api/admin/transactions?page=${page}&size=${size}`, token);
}

export async function fetchAdminAuditLogs(
  token: string | null,
  page = 0,
  size = 50
): Promise<SpringPage<AdminAuditRow>> {
  return apiFetch<SpringPage<AdminAuditRow>>(`/api/admin/audit-logs?page=${page}&size=${size}`, token);
}

export async function banAdminUser(token: string | null, userId: number, reason: string): Promise<void> {
  await apiFetch<unknown>(`/api/admin/users/${userId}/ban`, token, jsonInit({ reason }, { method: 'PUT' }));
}

export async function changeAdminUserRole(token: string | null, userId: number, role: string): Promise<void> {
  await apiFetch<unknown>(`/api/admin/users/${userId}/role`, token, jsonInit({ role }, { method: 'PUT' }));
}

export async function adminAccountDeposit(
  token: string | null,
  accountId: number,
  amount: number,
  description: string
): Promise<void> {
  await apiFetch<unknown>(
    `/api/admin/accounts/${accountId}/deposit`,
    token,
    jsonInit({ amount, description }, { method: 'POST' })
  );
}

export async function adminAccountWithdraw(
  token: string | null,
  accountId: number,
  amount: number,
  description: string
): Promise<void> {
  await apiFetch<unknown>(
    `/api/admin/accounts/${accountId}/withdraw`,
    token,
    jsonInit({ amount, description }, { method: 'POST' })
  );
}

export async function changeAccountStatus(
  token: string | null,
  accountId: number,
  status: 'ACTIVE' | 'BLOCKED' | 'CLOSED',
  reason: string
): Promise<void> {
  await apiFetch<unknown>(
    `/api/admin/accounts/${accountId}/status`,
    token,
    jsonInit({ status, reason }, { method: 'PUT' })
  );
}

export async function changeCardStatus(
  token: string | null,
  cardId: number,
  status: 'BLOCKED' | 'ACTIVE',
  reason: string
): Promise<void> {
  await apiFetch<unknown>(
    `/api/admin/cards/${cardId}/status`,
    token,
    jsonInit({ status, reason }, { method: 'PUT' })
  );
}
