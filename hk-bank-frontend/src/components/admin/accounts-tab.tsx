import { useCallback, useEffect, useState } from 'react';
import { Lock } from 'lucide-react';
import { toast } from 'sonner';
import { DataTable, type Column, type RowAction } from '@/components/admin/data-table';
import { DepositWithdrawModal, ActionModal } from '@/components/admin/modals';
import { SuccessAnimation } from '@/components/admin/success-animation';
import { useAuth } from '@/context/auth-context';
import {
  adminAccountDeposit,
  adminAccountWithdraw,
  changeAccountStatus,
  fetchAdminAccounts,
  normalizePage,
} from '@/lib/admin-api';
import type { AdminAccountRow } from '@/lib/admin-types';

type Row = AdminAccountRow & Record<string, unknown>;

function num(v: string | number | undefined | null): number {
  if (v === undefined || v === null) return 0;
  const n = typeof v === 'number' ? v : parseFloat(String(v));
  return Number.isFinite(n) ? n : 0;
}

export function AccountsTab() {
  const { token } = useAuth();
  const [rows, setRows] = useState<Row[]>([]);
  const [dataLoading, setDataLoading] = useState(true);
  const [selected, setSelected] = useState<Row | null>(null);
  const [depositOpen, setDepositOpen] = useState(false);
  const [freezeOpen, setFreezeOpen] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [success, setSuccess] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  const fetchRows = useCallback(async () => {
    try {
      setDataLoading(true);
      const page = await fetchAdminAccounts(token, 0, 100);
      setRows(normalizePage(page).items as Row[]);
    } catch {
      toast.error('Failed to load accounts');
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
      key: 'accountNumber',
      label: 'Account',
      render: (v) => <span className="font-mono text-white">{String(v ?? '—')}</span>,
    },
    {
      key: 'currencyType',
      label: 'Currency',
      render: (v) => (
        <span className="rounded-full bg-blue-500/20 px-3 py-1 text-xs font-medium text-blue-300">{String(v ?? '—')}</span>
      ),
    },
    {
      key: 'balance',
      label: 'Balance',
      render: (v, row) => (
        <span className="font-medium text-emerald-400">
          {num(v as string | number).toLocaleString()} {row.currencyType ?? ''}
        </span>
      ),
    },
    {
      key: 'status',
      label: 'Status',
      render: (v) => {
        const s = String(v ?? '').toLowerCase();
        const cls =
          s === 'active'
            ? 'bg-emerald-500/20 text-emerald-400'
            : s === 'blocked'
              ? 'bg-amber-500/20 text-amber-400'
              : 'bg-red-500/20 text-red-400';
        return <span className={`rounded-full px-3 py-1 text-xs font-medium capitalize ${cls}`}>{String(v ?? '—')}</span>;
      },
    },
    {
      key: 'createdAt',
      label: 'Created',
      render: (v) => (
        <span className="text-sm text-slate-400">{v ? new Date(String(v)).toLocaleDateString() : '—'}</span>
      ),
    },
  ];

  const actions: RowAction<Row>[] = [
    {
      label: 'Deposit / Withdraw',
      onClick: (r) => {
        setSelected(r);
        setDepositOpen(true);
      },
    },
    {
      label: 'Block',
      onClick: (r) => {
        setSelected(r);
        setFreezeOpen(true);
      },
      icon: <Lock className="h-4 w-4" />,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-white">Accounts</h2>
        <p className="text-slate-400">Deposit, withdraw, or block accounts</p>
      </div>
      <div className="rounded-xl border border-white/10 bg-white/5 p-6 shadow-lg backdrop-blur-xl">
        <DataTable<Row> columns={columns} data={rows} actions={actions} isLoading={dataLoading} emptyMessage="No accounts" />
      </div>

      <DepositWithdrawModal
        isOpen={depositOpen}
        onClose={() => setDepositOpen(false)}
        isLoading={processing}
        accountLabel={selected?.accountNumber ? String(selected.accountNumber) : undefined}
        onSubmit={async (amount, type) => {
          if (!selected) return;
          setProcessing(true);
          const desc = type === 'deposit' ? 'Admin panel deposit' : 'Admin panel withdrawal';
          try {
            if (type === 'deposit') {
              await adminAccountDeposit(token, selected.id, amount, desc);
            } else {
              await adminAccountWithdraw(token, selected.id, amount, desc);
            }
            setSuccessMsg(type === 'deposit' ? 'Deposit completed' : 'Withdrawal completed');
            setSuccess(true);
            setDepositOpen(false);
            await fetchRows();
          } catch {
            toast.error('Operation failed');
          } finally {
            setProcessing(false);
          }
        }}
      />

      <ActionModal
        isOpen={freezeOpen}
        onClose={() => setFreezeOpen(false)}
        isLoading={processing}
        title={`Block account — ${selected?.accountNumber ?? ''}`}
        fields={[
          {
            name: 'reason',
            label: 'Reason',
            type: 'textarea',
            placeholder: 'Reason for blocking this account…',
          },
        ]}
        onSubmit={async (data) => {
          if (!selected) return;
          setProcessing(true);
          try {
            await changeAccountStatus(token, selected.id, 'BLOCKED', data.reason);
            setSuccessMsg('Account blocked');
            setSuccess(true);
            setFreezeOpen(false);
            await fetchRows();
          } catch {
            toast.error('Could not update account');
          } finally {
            setProcessing(false);
          }
        }}
      />

      <SuccessAnimation
        isVisible={success}
        message={successMsg}
        celebrate
        onComplete={() => setSuccess(false)}
      />
    </div>
  );
}
