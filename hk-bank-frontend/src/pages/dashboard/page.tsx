import { DashboardLayout } from '@/components/dashboard/layout';
import { StatsCard } from '@/components/dashboard/stats-card';
import { Accounts } from '@/components/dashboard/accounts';
import { SpendingChart } from '@/components/dashboard/spending-chart';
import { RecentTransactions } from '@/components/dashboard/recent-transactions';
import { ProtectedRoute } from '@/components/protected-route';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { useAuth } from '@/context/auth-context';
import { useDashboardData } from '@/hooks/use-dashboard-data';
import { motion } from 'framer-motion';

function DashboardContent() {
  const { user } = useAuth();
  const { stats, accounts, transactions, chartData, isLoading, error, isAdmin } =
    useDashboardData();

  const displayName =
    [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.email || 'there';

  return (
    <DashboardLayout isAdmin={isAdmin}>
      {error ? (
        <Alert variant="destructive" className="mb-6 border-red-500/40 bg-red-950/30">
          <AlertTitle>Something went wrong</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      ) : null}

      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-8"
      >
        <h1 className="text-3xl md:text-4xl font-bold text-white mb-2">
          Welcome back, {displayName}
        </h1>
        <p className="text-slate-400">Here&apos;s what&apos;s happening with your account today.</p>
      </motion.div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {stats.map((stat, index) => (
          <StatsCard
            key={stat.title}
            title={stat.title}
            value={stat.value}
            change={stat.change}
            icon={stat.icon}
            index={index}
            showChange={stat.showChange !== false}
            valueSuffix={stat.valueSuffix}
          />
        ))}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-5 gap-6">
        <SpendingChart data={chartData} isLoading={isLoading} />
        <Accounts accounts={accounts} isLoading={isLoading} />
        <RecentTransactions transactions={transactions} isLoading={isLoading} />
      </div>
    </DashboardLayout>
  );
}

export default function DashboardPage() {
  return (
    <ProtectedRoute>
      <DashboardContent />
    </ProtectedRoute>
  );
}
