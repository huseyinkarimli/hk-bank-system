import { useState } from 'react';
import { AdminLayout, type AdminTabId } from '@/components/admin/admin-layout';
import { AdminOverview } from '@/components/admin/admin-overview';
import { UsersTab } from '@/components/admin/users-tab';
import { AccountsTab } from '@/components/admin/accounts-tab';
import { CardsTab } from '@/components/admin/cards-tab';
import { TransactionsTab } from '@/components/admin/transactions-tab';
import { AuditLogsTab } from '@/components/admin/audit-logs-tab';

export default function AdminPage() {
  const [tab, setTab] = useState<AdminTabId>('overview');

  return (
    <AdminLayout title="İnzibatçı paneli" activeTab={tab} onTabChange={setTab}>
      {tab === 'overview' ? <AdminOverview /> : null}
      {tab === 'users' ? <UsersTab /> : null}
      {tab === 'accounts' ? <AccountsTab /> : null}
      {tab === 'cards' ? <CardsTab /> : null}
      {tab === 'transactions' ? <TransactionsTab /> : null}
      {tab === 'audit' ? <AuditLogsTab /> : null}
    </AdminLayout>
  );
}
