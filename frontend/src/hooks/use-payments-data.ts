import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '@/context/auth-context';
import { apiClient } from '@/lib/axios-client';

export interface PaymentAccountOption {
  id: string;
  balance: number;
  currency: string;
  label: string;
}

export interface ProviderRow {
  providerType: string;
  providerName: string;
}

interface AccountApi {
  id: number;
  balance: string | number;
  currencyType: string;
  accountNumber?: string;
}

interface ProviderListApi {
  providerType: string;
  providers: string[];
}

interface PaymentSummaryApi {
  id: number;
  referenceNumber?: string;
  providerType: string;
  providerName: string;
  amount: string | number;
  status: string;
  createdAt: string;
}

function num(v: string | number | undefined | null): number {
  if (v === undefined || v === null) return 0;
  const n = typeof v === 'number' ? v : parseFloat(String(v));
  return Number.isFinite(n) ? n : 0;
}

export function flattenProviders(rows: ProviderListApi[]): ProviderRow[] {
  const out: ProviderRow[] = [];
  for (const r of rows) {
    for (const name of r.providers ?? []) {
      out.push({ providerType: r.providerType, providerName: name });
    }
  }
  return out;
}

export function usePaymentsPageData() {
  const { token } = useAuth();
  const [accounts, setAccounts] = useState<PaymentAccountOption[]>([]);
  const [providers, setProviders] = useState<ProviderRow[]>([]);
  const [payments, setPayments] = useState<PaymentSummaryApi[]>([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    if (!token) {
      setAccounts([]);
      setProviders([]);
      setPayments([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const [accRes, provRes, payRes] = await Promise.all([
        apiClient.get<AccountApi[]>('/api/accounts'),
        apiClient.get<ProviderListApi[]>('/api/payments/providers'),
        apiClient.get<PaymentSummaryApi[]>('/api/payments'),
      ]);

      setAccounts(
        (accRes.data ?? []).map((a) => ({
          id: String(a.id),
          balance: num(a.balance),
          currency: a.currencyType || 'AZN',
          label: a.accountNumber
            ? `Hesab · ${String(a.accountNumber).slice(-4)}`
            : `Hesab ${a.id}`,
        }))
      );

      setProviders(flattenProviders(provRes.data ?? []));
      setPayments(payRes.data ?? []);
    } catch {
      setAccounts([]);
      setProviders([]);
      setPayments([]);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void load();
  }, [load]);

  return { accounts, providers, payments, loading, refetch: load };
}
