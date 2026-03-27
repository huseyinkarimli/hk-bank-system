import { useCallback, useEffect, useState } from 'react';
import { toast } from 'sonner';
import { DashboardLayout } from '@/components/dashboard/layout';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Plus } from 'lucide-react';
import {
  AccountCard,
  type AccountCardModel,
} from '@/components/accounts/AccountCard';
import { CreateAccountModal } from '@/components/accounts/CreateAccountModal';
import { StatementModal } from '@/components/accounts/StatementModal';
import { api } from '@/lib/axios';
import { useAuth } from '@/context/auth-context';

interface ApiAccountSummary {
  id: number;
  iban: string;
  balance: number;
  currencyType: string;
  status: string;
}

function mapAccount(a: ApiAccountSummary): AccountCardModel {
  return {
    id: String(a.id),
    currency: a.currencyType as AccountCardModel['currency'],
    balance: Number(a.balance),
    iban: a.iban,
    status: a.status as AccountCardModel['status'],
  };
}

function AccountsContent() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const [accounts, setAccounts] = useState<AccountCardModel[]>([]);
  const [loading, setLoading] = useState(true);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [statementFor, setStatementFor] = useState<AccountCardModel | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get<{ data?: ApiAccountSummary[] }>('/api/accounts');
      const list = Array.isArray(data?.data) ? data.data : [];
      setAccounts(list.map(mapAccount));
    } catch {
      setAccounts([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const handleCreateAccount = async (currency: string) => {
    try {
      await api.post('/api/accounts', { currencyType: currency });
      toast.success('Hesab uğurla yaradıldı');
      setIsCreateModalOpen(false);
      void load();
    } catch {
      /* interceptor */
    }
  };

  const handleDeleteAccount = async (accountId: string) => {
    try {
      await api.delete(`/api/accounts/${accountId}`);
      toast.success('Hesab silindi');
      void load();
    } catch {
      /* interceptor */
    }
  };

  const existingCurrencies = accounts.map((a) => a.currency);

  return (
    <DashboardLayout isAdmin={isAdmin}>
      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl md:text-4xl font-bold text-white mb-2">Hesablarım</h1>
            <p className="text-slate-400">
              {loading ? 'Yüklənir…' : `${accounts.length} hesab`}
            </p>
          </div>
          <Button
            onClick={() => setIsCreateModalOpen(true)}
            disabled={accounts.length >= 3 || loading}
            className="bg-cyan-500 hover:bg-cyan-600 text-slate-950 gap-2"
          >
            <Plus size={18} />
            Yeni hesab aç
          </Button>
        </div>

        {loading ? (
          <p className="text-slate-400">Hesablar yüklənir…</p>
        ) : accounts.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {accounts.map((account) => (
              <AccountCard
                key={account.id}
                account={account}
                onStatement={(a) => setStatementFor(a)}
                onDelete={handleDeleteAccount}
              />
            ))}
          </div>
        ) : (
          <Card className="p-12 text-center bg-slate-900/40 border-slate-700/50">
            <div className="space-y-4">
              <p className="text-2xl">🏦</p>
              <p className="text-lg font-semibold text-white">Heç bir hesab yoxdur</p>
              <p className="text-slate-400 mb-4">Başlamaq üçün yeni hesab açın</p>
              <Button
                onClick={() => setIsCreateModalOpen(true)}
                className="gap-2"
                disabled={accounts.length >= 3}
              >
                <Plus size={16} />
                Birinci hesabı aç
              </Button>
            </div>
          </Card>
        )}

        <CreateAccountModal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          onConfirm={(c) => void handleCreateAccount(c)}
          existingCurrencies={existingCurrencies}
        />

        {statementFor ? (
          <StatementModal
            open={!!statementFor}
            onOpenChange={(open) => !open && setStatementFor(null)}
            accountId={statementFor.id}
            currency={statementFor.currency}
          />
        ) : null}
      </div>
    </DashboardLayout>
  );
}

export default function AccountsPage() {
  return <AccountsContent />;
}
