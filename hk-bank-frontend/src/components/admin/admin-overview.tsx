import { useEffect, useMemo, useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { Users, UserX, CreditCard, Ban, TrendingUp, Wallet } from 'lucide-react';
import { toast } from 'sonner';
import { StatsCard } from '@/components/admin/stats-card';
import { AdminCardSkeleton, ChartSkeleton } from '@/components/ui/skeleton';
import { useAuth } from '@/context/auth-context';
import {
  fetchAdminDashboardStats,
  fetchAdminTransactionStats,
} from '@/lib/admin-api';
import type { AdminDashboardStats, AdminTransactionStats } from '@/lib/admin-types';

function num(v: string | number | undefined | null): number {
  if (v === undefined || v === null) return 0;
  const n = typeof v === 'number' ? v : parseFloat(String(v));
  return Number.isFinite(n) ? n : 0;
}

const chartTooltip = {
  contentStyle: {
    backgroundColor: 'rgba(15, 23, 42, 0.92)',
    border: '1px solid rgba(255, 255, 255, 0.12)',
    borderRadius: 12,
    boxShadow: '0 8px 32px rgba(0,0,0,0.4)',
  },
  labelStyle: { color: '#e2e8f0' },
  itemStyle: { color: '#cbd5e1' },
};

const PIE_COLORS = ['#22d3ee', '#38bdf8', '#818cf8', '#34d399', '#fbbf24', '#f472b6'];

export function AdminOverview() {
  const { token } = useAuth();
  const [stats, setStats] = useState<AdminDashboardStats | null>(null);
  const [txStats, setTxStats] = useState<AdminTransactionStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        setLoading(true);
        const [s, t] = await Promise.all([
          fetchAdminDashboardStats(token),
          fetchAdminTransactionStats(token),
        ]);
        if (!cancelled) {
          setStats(s);
          setTxStats(t);
        }
      } catch {
        if (!cancelled) {
          toast.error('Failed to load admin dashboard data');
          setStats(null);
          setTxStats(null);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [token]);

  const barData = useMemo(() => {
    const vol = txStats?.dailyVolume;
    if (!vol || typeof vol !== 'object') return [];
    const keys = Object.keys(vol).sort();
    const last = keys.slice(-7);
    return last.map((date) => ({
      date: date.slice(5),
      volume: num(vol[date]),
    }));
  }, [txStats]);

  const pieData = useMemo(() => {
    const m = txStats?.countByStatus;
    if (!m) return [];
    return Object.entries(m).map(([name, value]) => ({
      name: name.replace(/_/g, ' '),
      value: Number(value) || 0,
    }));
  }, [txStats]);

  if (loading) {
    return (
      <div className="space-y-6">
        <AdminCardSkeleton count={6} />
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <ChartSkeleton />
          <ChartSkeleton />
        </div>
      </div>
    );
  }

  const totalBal = stats ? num(stats.totalBalanceInAzn) : 0;

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
        <StatsCard title="Total Users" value={(stats?.totalUsers ?? 0).toLocaleString()} icon={Users} delay={0} color="blue" />
        <StatsCard title="Banned Users" value={(stats?.bannedUsers ?? 0).toLocaleString()} icon={UserX} delay={0.05} color="red" />
        <StatsCard
          title="Active Cards"
          value={(stats?.totalActiveCards ?? 0).toLocaleString()}
          icon={CreditCard}
          delay={0.1}
          color="cyan"
        />
        <StatsCard
          title="Blocked Cards"
          value={(stats?.totalBlockedCards ?? 0).toLocaleString()}
          icon={Ban}
          delay={0.15}
          color="orange"
        />
        <StatsCard
          title="Transactions Today"
          value={(stats?.todayTransactionCount ?? 0).toLocaleString()}
          icon={TrendingUp}
          delay={0.2}
          color="purple"
        />
        <StatsCard
          title="Bankdakı ümumi pul"
          value={`${totalBal.toLocaleString()} AZN`}
          icon={Wallet}
          delay={0.25}
          color="green"
          sensitive
        />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="rounded-xl border border-white/10 bg-white/5 p-6 shadow-xl shadow-black/20 backdrop-blur-xl">
          <h3 className="mb-4 text-lg font-semibold text-white">Daily volume (recent days)</h3>
          {barData.length === 0 ? (
            <p className="py-12 text-center text-slate-500">No transaction volume data yet</p>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={barData}>
                <defs>
                  <linearGradient id="adminBarVol" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#22d3ee" stopOpacity={0.95} />
                    <stop offset="100%" stopColor="#2563eb" stopOpacity={0.35} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" vertical={false} />
                <XAxis dataKey="date" stroke="rgba(226,232,240,0.5)" tick={{ fill: 'rgba(226,232,240,0.65)', fontSize: 12 }} />
                <YAxis stroke="rgba(226,232,240,0.5)" tick={{ fill: 'rgba(226,232,240,0.65)', fontSize: 12 }} />
                <Tooltip {...chartTooltip} />
                <Legend wrapperStyle={{ color: '#94a3b8' }} />
                <Bar dataKey="volume" name="Volume (AZN)" fill="url(#adminBarVol)" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="rounded-xl border border-white/10 bg-white/5 p-6 shadow-xl shadow-black/20 backdrop-blur-xl">
          <h3 className="mb-4 text-lg font-semibold text-white">Transactions by status</h3>
          {pieData.length === 0 ? (
            <p className="py-12 text-center text-slate-500">No status breakdown yet</p>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={(props) => {
                    const name = String(props.name ?? '');
                    const pct = ((props.percent ?? 0) * 100).toFixed(0);
                    return `${name} ${pct}%`;
                  }}
                  outerRadius={96}
                  dataKey="value"
                  stroke="rgba(255,255,255,0.08)"
                  strokeWidth={1}
                >
                  {pieData.map((_, i) => (
                    <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip {...chartTooltip} />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    </div>
  );
}
