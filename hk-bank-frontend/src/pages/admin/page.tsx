import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ProtectedRoute } from '@/components/protected-route';
import { AdminLayout, type AdminTabId } from '@/components/admin/admin-layout';
import { AdminOverview } from '@/components/admin/admin-overview';
import { UsersTab } from '@/components/admin/users-tab';
import { AccountsTab } from '@/components/admin/accounts-tab';
import { CardsTab } from '@/components/admin/cards-tab';
import { TransactionsTab } from '@/components/admin/transactions-tab';
import { AuditLogsTab } from '@/components/admin/audit-logs-tab';
import { useAuth } from '@/context/auth-context';

function AdminContent() {
  const { user, isLoading } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState<AdminTabId>('overview');

  useEffect(() => {
    if (isLoading) return;
    if (user?.role !== 'ADMIN') {
      navigate('/dashboard', { replace: true });
    }
  }, [isLoading, user, navigate]);

  if (isLoading) {
    return <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950" />;
  }

  if (user?.role !== 'ADMIN') {
    return null;
  }

  return (
    <AdminLayout title="Admin Panel" activeTab={tab} onTabChange={setTab}>
      {tab === 'overview' ? <AdminOverview /> : null}
      {tab === 'users' ? <UsersTab /> : null}
      {tab === 'accounts' ? <AccountsTab /> : null}
      {tab === 'cards' ? <CardsTab /> : null}
      {tab === 'transactions' ? <TransactionsTab /> : null}
      {tab === 'audit' ? <AuditLogsTab /> : null}
    </AdminLayout>
  );
}

export default function AdminPage() {
  return (
    <ProtectedRoute>
      <AdminContent />
    </ProtectedRoute>
  );
}
