import { useCallback, useEffect, useState } from 'react';
import { toast } from 'sonner';
import { DataTable, type Column } from '@/components/admin/data-table';
import { useAuth } from '@/context/auth-context';
import { fetchAdminTransactions, normalizePage } from '@/lib/admin-api';
import type { AdminTransactionRow } from '@/lib/admin-types';

type Row = AdminTransactionRow & Record<string, unknown>;

function num(v: string | number | undefined | null): number {
  if (v === undefined || v === null) return 0;
  const n = typeof v === 'number' ? v : parseFloat(String(v));
  return Number.isFinite(n) ? n : 0;
}

function typeStyle(t: string) {
  const u = t.toUpperCase();
  if (u.includes('DEPOSIT')) return 'bg-emerald-500/20 text-emerald-400';
  if (u.includes('WITHDRAW')) return 'bg-red-500/20 text-red-400';
  if (u.includes('P2P')) return 'bg-blue-500/20 text-blue-400';
  return 'bg-violet-500/20 text-violet-400';
}

export function TransactionsTab() {
  const { token } = useAuth();
  const [rows, setRows] = useState<Row[]>([]);
  const [dataLoading, setDataLoading] = useState(true);

  const fetchRows = useCallback(async () => {
    try {
      setDataLoading(true);
      const page = await fetchAdminTransactions(token, 0, 100);
      setRows(normalizePage(page).items as Row[]);
    } catch {
      toast.error('Failed to load transactions');
      setRows([]);
    } finally {
      setDataLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void fetchRows();
  }, [fetchRows]);

  const columns: Column<Row>[] = [
    {
      key: 'referenceNumber',
      label: 'Reference',
      render: (v) => <span className="font-mono text-white">{String(v ?? '—')}</span>,
    },
    {
      key: 'type',
      label: 'Type',
      render: (v) => {
        const s = String(v ?? '');
        return (
          <span className={`rounded-full px-3 py-1 text-xs font-medium ${typeStyle(s)}`}>
            {s.replace(/_/g, ' ')}
          </span>
        );
      },
    },
    {
      key: 'amount',
      label: 'Amount',
      render: (v, row) => {
        const amount = num(v as string | number);
        const t = String(row.type ?? '').toUpperCase();
        const isDeposit = t === 'DEPOSIT';
        const isWithdraw = t === 'WITHDRAWAL';
        const sign = isDeposit ? '+' : isWithdraw ? '−' : '';
        const color = isDeposit ? 'text-emerald-400' : isWithdraw ? 'text-red-400' : 'text-slate-200';
        return (
          <span className={`font-medium ${color}`}>
            {sign}
            {amount.toLocaleString()} {row.sourceCurrency ? String(row.sourceCurrency) : ''}
          </span>
        );
      },
    },
    {
      key: 'status',
      label: 'Status',
      render: (v) => {
        const s = String(v ?? '').toLowerCase();
        const cls =
          s === 'success' || s === 'completed'
            ? 'bg-emerald-500/20 text-emerald-400'
            : s === 'pending'
              ? 'bg-amber-500/20 text-amber-400'
              : 'bg-red-500/20 text-red-400';
        return <span className={`rounded-full px-3 py-1 text-xs font-medium capitalize ${cls}`}>{String(v ?? '—')}</span>;
      },
    },
    {
      key: 'createdAt',
      label: 'Date',
      render: (v) => (
        <div className="text-sm text-slate-400">
          <p>{v ? new Date(String(v)).toLocaleDateString() : '—'}</p>
          <p className="text-xs text-slate-500">{v ? new Date(String(v)).toLocaleTimeString() : ''}</p>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-white">Transactions</h2>
        <p className="text-slate-400">Read-only list (no refund API on backend)</p>
      </div>
      <div className="rounded-xl border border-white/10 bg-white/5 p-6 shadow-lg backdrop-blur-xl">
        <DataTable<Row> columns={columns} data={rows} isLoading={dataLoading} emptyMessage="No transactions" />
      </div>
    </div>
  );
}
