import { useCallback, useEffect, useState } from 'react';
import { toast } from 'sonner';
import { DataTable, type Column } from '@/components/admin/data-table';
import { useAuth } from '@/context/auth-context';
import { fetchAdminAuditLogs, normalizePage } from '@/lib/admin-api';
import type { AdminAuditRow } from '@/lib/admin-types';

type Row = AdminAuditRow & Record<string, unknown>;

function actionColor(action: string) {
  const a = action.toLowerCase();
  if (a.includes('ban') || a.includes('block') || a.includes('delete')) return 'bg-red-500/20 text-red-400';
  if (a.includes('create') || a.includes('add')) return 'bg-emerald-500/20 text-emerald-400';
  if (a.includes('update') || a.includes('change') || a.includes('role')) return 'bg-blue-500/20 text-blue-400';
  return 'bg-amber-500/20 text-amber-400';
}

export function AuditLogsTab() {
  const { token } = useAuth();
  const [rows, setRows] = useState<Row[]>([]);
  const [dataLoading, setDataLoading] = useState(true);

  const fetchRows = useCallback(async () => {
    try {
      setDataLoading(true);
      const page = await fetchAdminAuditLogs(token, 0, 200);
      setRows(normalizePage(page).items as Row[]);
    } catch {
      toast.error('Failed to load audit logs');
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
      key: 'createdAt',
      label: 'Date & time',
      render: (v) => (
        <div className="text-sm text-slate-400">
          <p>{v ? new Date(String(v)).toLocaleDateString() : '—'}</p>
          <p className="text-xs text-slate-500">{v ? new Date(String(v)).toLocaleTimeString() : ''}</p>
        </div>
      ),
    },
    {
      key: 'userId',
      label: 'User ID',
      render: (v) => <span className="font-mono text-white">{String(v ?? '—')}</span>,
    },
    {
      key: 'action',
      label: 'Action',
      render: (v) => {
        const s = String(v ?? '');
        return (
          <span className={`rounded-full px-3 py-1 text-xs font-medium ${actionColor(s)}`}>
            {s.replace(/_/g, ' ')}
          </span>
        );
      },
    },
    {
      key: 'entityType',
      label: 'Target type',
      render: (v) => (
        <span className="rounded-full bg-violet-500/20 px-3 py-1 text-xs font-medium text-violet-300 capitalize">
          {String(v ?? '—')}
        </span>
      ),
    },
    {
      key: 'entityId',
      label: 'Target ID',
      render: (v) => <span className="font-mono text-slate-300">{String(v ?? '—')}</span>,
    },
    {
      key: 'ipAddress',
      label: 'IP',
      render: (v) => <span className="font-mono text-sm text-slate-500">{String(v ?? '—')}</span>,
    },
    {
      key: 'description',
      label: 'Description',
      render: (v) => <span className="max-w-xs truncate text-slate-400">{String(v ?? '—')}</span>,
    },
  ];

  const byType = (t: string) => rows.filter((r) => String(r.entityType ?? '').toLowerCase() === t.toLowerCase()).length;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-white">Audit logs</h2>
        <p className="text-slate-400">Administrative and security events</p>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        {[
          { label: 'Total (page)', value: rows.length },
          { label: 'User entity', value: byType('USER') },
          { label: 'Account entity', value: byType('ACCOUNT') },
          { label: 'Transaction entity', value: byType('TRANSACTION') },
        ].map((c) => (
          <div
            key={c.label}
            className="rounded-xl border border-white/10 bg-white/5 p-4 shadow-lg backdrop-blur-xl"
          >
            <p className="mb-1 text-sm text-slate-400">{c.label}</p>
            <p className="text-2xl font-bold text-white">{c.value}</p>
          </div>
        ))}
      </div>

      <div className="rounded-xl border border-white/10 bg-white/5 p-6 shadow-lg backdrop-blur-xl">
        <DataTable<Row> columns={columns} data={rows} isLoading={dataLoading} emptyMessage="No audit logs" />
      </div>
    </div>
  );
}
