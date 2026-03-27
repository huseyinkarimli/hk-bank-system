import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '@/context/auth-context';
import { apiFetch } from '@/lib/api';

export interface DashboardStat {
  title: string;
  value: number;
  change: number;
  icon: string;
  showChange?: boolean;
  valueSuffix?: string;
}

export interface Account {
  id: string;
  name: string;
  type: string;
  balance: number;
  currency: string;
  lastTransaction: string;
}

export interface Transaction {
  id: string;
  description: string;
  amount: number;
  type: 'debit' | 'credit';
  timestamp: string;
  category: string;
  currency: string;
}

export interface ChartDataPoint {
  day: string;
  spending: number;
}

interface AccountSummaryApi {
  id: number;
  accountNumber?: string;
  iban?: string;
  balance: string | number;
  currencyType: string;
  status: string;
}

interface CardSummaryApi {
  id: number;
  status: string;
}

interface TransactionSummaryApi {
  id: number;
  referenceNumber?: string;
  type: string;
  status: string;
  amount: string | number;
  sourceCurrency?: string;
  createdAt: string;
}

interface SpringPage<T> {
  content: T[];
}

interface BalanceSummaryApi {
  [currency: string]: string | number;
}

interface AdminDashboardStatsApi {
  totalUsers: number;
  bannedUsers: number;
  totalActiveCards: number;
  totalBlockedCards: number;
  todayTransactionCount: number;
  todayTransactionVolume: string | number;
  totalBalanceInAzn: string | number;
}

const DAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

function num(v: string | number | undefined | null): number {
  if (v === undefined || v === null) return 0;
  const n = typeof v === 'number' ? v : parseFloat(String(v));
  return Number.isFinite(n) ? n : 0;
}

function isCreditType(type: string): boolean {
  return type === 'DEPOSIT';
}

function formatTxTime(iso: string): string {
  try {
    const d = new Date(iso);
    return d.toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
  } catch {
    return iso;
  }
}

function startOfMonthIso(): string {
  const d = new Date();
  const s = new Date(d.getFullYear(), d.getMonth(), 1);
  return s.toISOString().slice(0, 19);
}

function buildWeeklyChart(rows: TransactionSummaryApi[]): ChartDataPoint[] {
  const now = new Date();
  const buckets: { label: string; outflow: number }[] = [];
  for (let i = 6; i >= 0; i--) {
    const day = new Date(now);
    day.setDate(day.getDate() - i);
    buckets.push({ label: DAYS[day.getDay()], outflow: 0 });
  }

  for (const tx of rows) {
    if (isCreditType(tx.type)) continue;
    const t = new Date(tx.createdAt);
    if (Number.isNaN(t.getTime())) continue;
    const diffDays = Math.floor((now.getTime() - t.getTime()) / 86400000);
    if (diffDays < 0 || diffDays > 6) continue;
    const idx = 6 - diffDays;
    if (buckets[idx]) buckets[idx].outflow += num(tx.amount);
  }

  return buckets.map((b) => ({ day: b.label, spending: b.outflow }));
}

export function useDashboardData() {
  const { token, user } = useAuth();
  const [stats, setStats] = useState<DashboardStat[]>([]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [chartData, setChartData] = useState<ChartDataPoint[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const isAdmin = user?.role === 'ADMIN';

  const load = useCallback(async () => {
    if (!token) {
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const [accountsRes, cardsRes, balanceRes, txPageRes, txMonthRes] = await Promise.allSettled([
        apiFetch<AccountSummaryApi[]>('/api/accounts', token),
        apiFetch<CardSummaryApi[]>('/api/cards', token),
        apiFetch<BalanceSummaryApi>('/api/accounts/balance-summary', token),
        apiFetch<SpringPage<TransactionSummaryApi>>('/api/transactions?size=50&sort=createdAt,desc', token),
        apiFetch<SpringPage<TransactionSummaryApi>>(
          `/api/transactions?size=200&sort=createdAt,desc&startDate=${encodeURIComponent(startOfMonthIso())}`,
          token
        ),
      ]);

      if (accountsRes.status === 'fulfilled') {
        const list = accountsRes.value;
        setAccounts(
          list.map((a) => ({
            id: String(a.id),
            name: a.accountNumber
              ? `Account · ${String(a.accountNumber).slice(-4)}`
              : `Account ${a.id}`,
            type: `${a.currencyType} · ${a.status}`,
            balance: num(a.balance),
            currency: a.currencyType,
            lastTransaction: '—',
          }))
        );
      } else {
        setAccounts([]);
      }

      let totalBalance = 0;
      if (balanceRes.status === 'fulfilled') {
        const summary = balanceRes.value;
        totalBalance = Object.values(summary).reduce((acc, v) => acc + num(v), 0);
      }

      let cardCount = 0;
      if (cardsRes.status === 'fulfilled') {
        cardCount = cardsRes.value.filter((c) => c.status === 'ACTIVE').length;
      }

      let monthlyOutflow = 0;
      if (txMonthRes.status === 'fulfilled') {
        for (const tx of txMonthRes.value.content ?? []) {
          if (!isCreditType(tx.type)) monthlyOutflow += num(tx.amount);
        }
      }

      let txRows: TransactionSummaryApi[] = [];
      if (txPageRes.status === 'fulfilled') {
        txRows = txPageRes.value.content ?? [];
        setTransactions(
          txRows.map((tx) => ({
            id: String(tx.id),
            description: tx.referenceNumber
              ? `${tx.type.replace(/_/g, ' ')} · ${tx.referenceNumber}`
              : tx.type.replace(/_/g, ' '),
            amount: num(tx.amount),
            type: isCreditType(tx.type) ? 'credit' : 'debit',
            timestamp: formatTxTime(tx.createdAt),
            category: tx.status,
            currency: tx.sourceCurrency ?? 'AZN',
          }))
        );
        setChartData(buildWeeklyChart(txRows));
      } else {
        setTransactions([]);
        const emptyChart: ChartDataPoint[] = [];
        for (let i = 6; i >= 0; i--) {
          const day = new Date();
          day.setDate(day.getDate() - i);
          emptyChart.push({ day: DAYS[day.getDay()], spending: 0 });
        }
        setChartData(emptyChart);
      }

      if (isAdmin) {
        try {
          const admin = await apiFetch<AdminDashboardStatsApi>(
            '/api/admin/dashboard/stats',
            token
          );
          setStats([
            {
              title: 'Total Users',
              value: admin.totalUsers,
              change: 0,
              icon: 'users',
              showChange: false,
            },
            {
              title: 'Active Cards',
              value: admin.totalActiveCards,
              change: 0,
              icon: 'credit-card',
              showChange: false,
            },
            {
              title: "Today's Transactions",
              value: admin.todayTransactionCount,
              change: 0,
              icon: 'activity',
              showChange: false,
            },
            {
              title: 'Total Balance (AZN)',
              value: Math.round(num(admin.totalBalanceInAzn)),
              change: 0,
              icon: 'landmark',
              showChange: false,
            },
          ]);
        } catch {
          setStats([]);
          setError('Could not load admin statistics.');
        }
      } else {
        const acctCount = accountsRes.status === 'fulfilled' ? accountsRes.value.length : 0;
        setStats([
          {
            title: 'Total Balance',
            value: Math.round(totalBalance),
            change: 0,
            icon: 'wallet',
            showChange: false,
            valueSuffix: 'AZN',
          },
          {
            title: 'Monthly Spending',
            value: Math.round(monthlyOutflow),
            change: 0,
            icon: 'trending-down',
            showChange: false,
            valueSuffix: 'AZN',
          },
          {
            title: 'Active Cards',
            value: cardCount,
            change: 0,
            icon: 'credit-card',
            showChange: false,
          },
          {
            title: 'Accounts',
            value: acctCount,
            change: 0,
            icon: 'building-2',
            showChange: false,
          },
        ]);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load dashboard');
    } finally {
      setIsLoading(false);
    }
  }, [token, isAdmin]);

  useEffect(() => {
    void load();
  }, [load]);

  return { stats, accounts, transactions, chartData, isLoading, error, isAdmin, refetch: load };
}
