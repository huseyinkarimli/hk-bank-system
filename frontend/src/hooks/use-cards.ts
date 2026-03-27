import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '@/context/auth-context';
import { apiFetch } from '@/lib/api';

export type UICardType = 'debit' | 'credit' | 'virtual';

export interface BankCardView {
  id: string;
  cardholderName: string;
  /** Masked PAN for default display */
  displayCardNumber: string;
  /** Raw 16 digits when API exposes full PAN (owner views) */
  panDigits?: string;
  cvv?: string | null;
  expiryLabel: string;
  cardType: UICardType;
  status: string;
  iban: string;
  balance: number;
  accountId: string;
  gradient: string;
  pattern?: string;
}

interface CardSummaryApi {
  id: number;
  maskedCardNumber: string;
  cardHolder: string;
  cardType: string;
  status: string;
  expiryDate?: string;
}

interface CardDetailApi {
  id: number;
  maskedCardNumber: string;
  fullCardNumber?: string;
  cvv?: string | null;
  cardHolder: string;
  cardType: string;
  status: string;
  expiryDate?: string;
  accountId: number;
}

interface AccountSummaryApi {
  id: number;
  balance: string | number;
  iban?: string;
  accountNumber?: string;
}

const GRADIENT_PRESETS = [
  'bg-gradient-to-br from-blue-600 to-blue-900',
  'bg-gradient-to-br from-purple-600 to-purple-900',
  'bg-gradient-to-br from-indigo-600 to-indigo-900',
  'bg-gradient-to-br from-slate-600 to-slate-900',
  'bg-gradient-to-br from-emerald-600 to-emerald-900',
  'bg-gradient-to-br from-rose-600 to-rose-900',
];

function num(v: string | number | undefined | null): number {
  if (v === undefined || v === null) return 0;
  const n = typeof v === 'number' ? v : parseFloat(String(v));
  return Number.isFinite(n) ? n : 0;
}

function mapCardType(t: string): UICardType {
  const u = String(t).toUpperCase();
  if (u === 'CREDIT') return 'credit';
  if (u === 'VIRTUAL') return 'virtual';
  return 'debit';
}

function formatExpiry(iso?: string): string {
  if (!iso) return '—';
  const part = iso.split('T')[0];
  const [y, m] = part.split('-');
  if (!y || !m) return '—';
  return `${m}/${y.slice(2)}`;
}

function digitsOnlyPan(formatted?: string): string | undefined {
  if (!formatted) return undefined;
  const d = formatted.replace(/\D/g, '');
  return d.length === 16 ? d : undefined;
}

function toView(
  detail: CardDetailApi,
  accounts: Map<string, AccountSummaryApi>,
  index: number,
  pattern?: string
): BankCardView {
  const acc = accounts.get(String(detail.accountId));
  const iban = acc?.iban ?? '—';
  const balance = num(acc?.balance);
  const panDigits = digitsOnlyPan(detail.fullCardNumber);
  return {
    id: String(detail.id),
    cardholderName: detail.cardHolder ?? '—',
    displayCardNumber: detail.maskedCardNumber ?? '—',
    panDigits,
    cvv: detail.cvv ?? null,
    expiryLabel: formatExpiry(detail.expiryDate),
    cardType: mapCardType(detail.cardType),
    status: String(detail.status).toUpperCase(),
    iban,
    balance,
    accountId: String(detail.accountId),
    gradient: GRADIENT_PRESETS[index % GRADIENT_PRESETS.length],
    pattern,
  };
}

export interface AccountOption {
  id: string;
  label: string;
}

export function useCards() {
  const { token } = useAuth();
  const [cards, setCards] = useState<BankCardView[]>([]);
  const [accounts, setAccounts] = useState<AccountOption[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!token) {
      setCards([]);
      setAccounts([]);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);

    let pattern = '';
    try {
      const raw = localStorage.getItem('cardCustomization');
      if (raw) pattern = JSON.parse(raw)?.pattern ?? '';
    } catch {
      pattern = '';
    }

    try {
      const [summaryList, accountList] = await Promise.all([
        apiFetch<CardSummaryApi[]>('/api/cards', token),
        apiFetch<AccountSummaryApi[]>('/api/accounts', token),
      ]);

      const accountMap = new Map<string, AccountSummaryApi>();
      for (const a of accountList) {
        accountMap.set(String(a.id), a);
      }

      setAccounts(
        accountList.map((a) => ({
          id: String(a.id),
          label: a.accountNumber
            ? `Account · ${String(a.accountNumber).slice(-4)}`
            : `Account ${a.id}`,
        }))
      );

      const details = await Promise.all(
        summaryList.map((s) =>
          apiFetch<CardDetailApi>(`/api/cards/${s.id}`, token).catch(() => null)
        )
      );

      const views: BankCardView[] = [];
      let i = 0;
      for (const d of details) {
        if (!d) continue;
        views.push(toView(d, accountMap, i, pattern || undefined));
        i += 1;
      }

      setCards(views);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load cards');
      setCards([]);
    } finally {
      setIsLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void load();
  }, [load]);

  const createCard = useCallback(
    async (accountId: string, cardType: UICardType, initialPin?: string) => {
      if (!token) throw new Error('Not signed in');
      const typeUpper = cardType.toUpperCase();
      const body: Record<string, unknown> = {
        accountId: Number(accountId),
        cardType: typeUpper,
      };
      if (initialPin && /^\d{4}$/.test(initialPin)) {
        body.initialPin = initialPin;
      }
      await apiFetch<CardDetailApi>('/api/cards', token, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      await load();
    },
    [token, load]
  );

  const updateCardStatus = useCallback(
    async (cardId: string, status: 'ACTIVE' | 'BLOCKED') => {
      if (!token) throw new Error('Not signed in');
      await apiFetch(`/api/cards/${cardId}/status`, token, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status }),
      });
      await load();
    },
    [token, load]
  );

  const deleteCard = useCallback(
    async (cardId: string) => {
      if (!token) throw new Error('Not signed in');
      await apiFetch(`/api/cards/${cardId}`, token, { method: 'DELETE' });
      await load();
    },
    [token, load]
  );

  const changePin = useCallback(
    async (cardId: string, currentPin: string, newPin: string) => {
      if (!token) throw new Error('Not signed in');
      await apiFetch(`/api/cards/${cardId}/pin`, token, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentPin, newPin }),
      });
    },
    [token]
  );

  return {
    cards,
    accounts,
    isLoading,
    error,
    refetch: load,
    createCard,
    updateCardStatus,
    deleteCard,
    changePin,
  };
}
