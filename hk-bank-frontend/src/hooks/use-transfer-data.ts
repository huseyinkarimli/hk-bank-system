import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '@/context/auth-context';
import { apiClient } from '@/lib/axios-client';

export interface TransferAccountOption {
  id: string;
  iban: string;
  balance: number;
  currency: string;
  label: string;
}

export interface TransferCardHint {
  id: string;
  maskedCardNumber: string;
}

interface AccountApi {
  id: number;
  iban?: string;
  balance: string | number;
  currencyType: string;
  accountNumber?: string;
}

interface CardSummaryApi {
  id: number;
  maskedCardNumber: string;
}

interface TransactionApi {
  id: number;
  referenceNumber?: string;
  type: string;
  amount: string | number;
  sourceCurrency?: string;
  createdAt: string;
}

interface SpringPage<T> {
  content: T[];
}

function num(v: string | number | undefined | null): number {
  if (v === undefined || v === null) return 0;
  const n = typeof v === 'number' ? v : parseFloat(String(v));
  return Number.isFinite(n) ? n : 0;
}

export function useTransferPageData() {
  const { token } = useAuth();
  const [accounts, setAccounts] = useState<TransferAccountOption[]>([]);
  const [cardHints, setCardHints] = useState<TransferCardHint[]>([]);
  const [transactions, setTransactions] = useState<TransactionApi[]>([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    if (!token) {
      setAccounts([]);
      setCardHints([]);
      setTransactions([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const [accRes, cardRes, txRes] = await Promise.all([
        apiClient.get<AccountApi[]>('/api/accounts'),
        apiClient.get<CardSummaryApi[]>('/api/cards'),
        apiClient.get<SpringPage<TransactionApi>>(
          '/api/transactions?size=12&sort=createdAt,desc'
        ),
      ]);

      const accList = accRes.data ?? [];
      setAccounts(
        accList.map((a) => ({
          id: String(a.id),
          iban: (a.iban ?? '').replace(/\s/g, ''),
          balance: num(a.balance),
          currency: a.currencyType || 'AZN',
          label: a.accountNumber
            ? `Hesab · ${String(a.accountNumber).slice(-4)}`
            : `Hesab ${a.id}`,
        }))
      );

      setCardHints(
        (cardRes.data ?? []).map((c) => ({
          id: String(c.id),
          maskedCardNumber: c.maskedCardNumber ?? '',
        }))
      );

      setTransactions(txRes.data?.content ?? []);
    } catch {
      setAccounts([]);
      setCardHints([]);
      setTransactions([]);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void load();
  }, [load]);

  return { accounts, cardHints, transactions, loading, refetch: load };
}
