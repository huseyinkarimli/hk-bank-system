import { useCallback, useEffect, useState } from 'react';
import { Shield } from 'lucide-react';
import { toast } from 'sonner';
import { DataTable, type Column, type RowAction } from '@/components/admin/data-table';
import { BanModal } from '@/components/admin/ban-modal';
import { ActionModal } from '@/components/admin/modals';
import { SuccessAnimation } from '@/components/admin/success-animation';
import { useAuth } from '@/context/auth-context';
import { banAdminUser, changeAdminUserRole, fetchAdminUsers, normalizePage } from '@/lib/admin-api';
import type { AdminUserRow } from '@/lib/admin-types';

type Row = AdminUserRow & Record<string, unknown>;

export function UsersTab() {
  const { token, user: authUser } = useAuth();
  const [users, setUsers] = useState<Row[]>([]);
  const [dataLoading, setDataLoading] = useState(true);
  const [selected, setSelected] = useState<Row | null>(null);
  const [roleOpen, setRoleOpen] = useState(false);
  const [banOpen, setBanOpen] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [success, setSuccess] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  const fetchUsers = useCallback(async () => {
    try {
      setDataLoading(true);
      const page = await fetchAdminUsers(token, 0, 100);
      const { items } = normalizePage(page);
      setUsers(items as Row[]);
    } catch {
      toast.error('Failed to load users');
      setUsers([]);
    } finally {
      setDataLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void fetchUsers();
  }, [fetchUsers]);

  const displayName = (u: Row) => {
    const n = [u.firstName, u.lastName].filter(Boolean).join(' ').trim();
    return n || u.email;
  };

  const columns: Column<Row>[] = [
    {
      key: 'id',
      label: 'User',
      render: (_, row) => (
        <div>
          <p className="font-medium text-white">{displayName(row)}</p>
          <p className="text-xs text-slate-400">{row.email}</p>
        </div>
      ),
    },
    {
      key: 'role',
      label: 'Role',
      render: (v) => (
        <span className="rounded-full bg-cyan-500/20 px-3 py-1 text-xs font-medium capitalize text-cyan-300">
          {String(v ?? '—')}
        </span>
      ),
    },
    {
      key: 'phoneNumber',
      label: 'Phone',
      render: (v) => <span className="text-slate-300">{String(v ?? '—')}</span>,
    },
    {
      key: 'createdAt',
      label: 'Created',
      render: (v) => (
        <span className="text-sm text-slate-400">
          {v ? new Date(String(v)).toLocaleDateString() : '—'}
        </span>
      ),
    },
  ];

  const actions: RowAction<Row>[] = [
    {
      label: 'Role',
      onClick: (u) => {
        setSelected(u);
        setRoleOpen(true);
      },
      icon: <Shield className="h-4 w-4" />,
    },
    {
      label: 'Ban',
      onClick: (u) => {
        setSelected(u);
        setBanOpen(true);
      },
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-white">Users</h2>
        <p className="text-slate-400">Manage platform users (ban, change role)</p>
      </div>
      <div className="rounded-xl border border-white/10 bg-white/5 p-6 shadow-lg backdrop-blur-xl">
        <DataTable<Row> columns={columns} data={users} actions={actions} isLoading={dataLoading} emptyMessage="No users" />
      </div>

      <ActionModal
        isOpen={roleOpen}
        onClose={() => setRoleOpen(false)}
        isLoading={processing}
        title={`Change role — ${selected ? displayName(selected) : ''}`}
        fields={[
          {
            name: 'role',
            label: 'New role',
            type: 'select',
            options: [
              { value: 'USER', label: 'User' },
              { value: 'ADMIN', label: 'Admin' },
              { value: 'AI_SUPPORT', label: 'AI Support' },
            ],
          },
        ]}
        onSubmit={async (data) => {
          if (!selected) return;
          setProcessing(true);
          try {
            await changeAdminUserRole(token, selected.id, data.role);
            setSuccessMsg('Role updated');
            setSuccess(true);
            setRoleOpen(false);
            await fetchUsers();
          } catch {
            toast.error('Could not change role');
          } finally {
            setProcessing(false);
          }
        }}
      />

      <BanModal
        isOpen={banOpen}
        onClose={() => setBanOpen(false)}
        isLoading={processing}
        userLabel={selected ? displayName(selected) : undefined}
        onSubmit={async (reason) => {
          if (!selected) return;
          if (authUser && String(selected.id) === authUser.id) {
            toast.error('You cannot ban your own account');
            return;
          }
          setProcessing(true);
          try {
            await banAdminUser(token, selected.id, reason);
            setSuccessMsg('User banned');
            setSuccess(true);
            setBanOpen(false);
            await fetchUsers();
          } catch {
            toast.error('Could not ban user');
          } finally {
            setProcessing(false);
          }
        }}
      />

      <SuccessAnimation isVisible={success} message={successMsg} onComplete={() => setSuccess(false)} />
    </div>
  );
}
