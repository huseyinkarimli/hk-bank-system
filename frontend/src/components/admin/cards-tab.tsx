import { useCallback, useEffect, useState } from 'react';
import { Lock } from 'lucide-react';
import { toast } from 'sonner';
import { DataTable, type Column, type RowAction } from '@/components/admin/data-table';
import { ActionModal } from '@/components/admin/modals';
import { SuccessAnimation } from '@/components/admin/success-animation';
import { useAuth } from '@/context/auth-context';
import { changeCardStatus, fetchAdminCards, normalizePage } from '@/lib/admin-api';
import type { AdminCardRow } from '@/lib/admin-types';

type Row = AdminCardRow & Record<string, unknown>;

export function CardsTab() {
  const { token } = useAuth();
  const [rows, setRows] = useState<Row[]>([]);
  const [dataLoading, setDataLoading] = useState(true);
  const [selected, setSelected] = useState<Row | null>(null);
  const [blockOpen, setBlockOpen] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [success, setSuccess] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  const fetchRows = useCallback(async () => {
    try {
      setDataLoading(true);
      const page = await fetchAdminCards(token, 0, 100);
      setRows(normalizePage(page).items as Row[]);
    } catch {
      toast.error('Failed to load cards');
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
      key: 'cardHolder',
      label: 'Cardholder',
      render: (v) => <span className="font-medium text-white">{String(v ?? '—')}</span>,
    },
    {
      key: 'maskedCardNumber',
      label: 'Number',
      render: (v) => <span className="font-mono text-slate-300">{String(v ?? '—')}</span>,
    },
    {
      key: 'cardType',
      label: 'Type',
      render: (v) => (
        <span className="rounded-full bg-violet-500/20 px-3 py-1 text-xs font-medium capitalize text-violet-300">
          {String(v ?? '—')}
        </span>
      ),
    },
    {
      key: 'expiryDate',
      label: 'Expiry',
      render: (v) => <span className="text-slate-300">{v ? String(v) : '—'}</span>,
    },
    {
      key: 'status',
      label: 'Status',
      render: (v) => {
        const s = String(v ?? '').toLowerCase();
        const cls =
          s === 'active'
            ? 'bg-emerald-500/20 text-emerald-400'
            : s === 'blocked' || s === 'frozen'
              ? 'bg-red-500/20 text-red-400'
              : 'bg-amber-500/20 text-amber-400';
        return <span className={`rounded-full px-3 py-1 text-xs font-medium capitalize ${cls}`}>{String(v ?? '—')}</span>;
      },
    },
  ];

  const actions: RowAction<Row>[] = [
    {
      label: 'Block',
      onClick: (r) => {
        setSelected(r);
        setBlockOpen(true);
      },
      icon: <Lock className="h-4 w-4" />,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-white">Cards</h2>
        <p className="text-slate-400">Block cards when needed</p>
      </div>
      <div className="rounded-xl border border-white/10 bg-white/5 p-6 shadow-lg backdrop-blur-xl">
        <DataTable<Row>
          columns={columns}
          data={rows}
          actions={actions}
          isLoading={dataLoading}
          emptyMessage="No cards"
        />
      </div>

      <ActionModal
        isOpen={blockOpen}
        onClose={() => setBlockOpen(false)}
        isLoading={processing}
        title={`Block card — ${selected?.cardHolder ?? ''}`}
        fields={[
          {
            name: 'reason',
            label: 'Reason',
            type: 'textarea',
            placeholder: 'Reason for blocking…',
          },
        ]}
        onSubmit={async (data) => {
          if (!selected) return;
          setProcessing(true);
          try {
            await changeCardStatus(token, selected.id, 'BLOCKED', data.reason);
            setSuccessMsg('Card blocked');
            setSuccess(true);
            setBlockOpen(false);
            await fetchRows();
          } catch {
            toast.error('Could not block card');
          } finally {
            setProcessing(false);
          }
        }}
      />

      <SuccessAnimation isVisible={success} message={successMsg} onComplete={() => setSuccess(false)} />
    </div>
  );
}
