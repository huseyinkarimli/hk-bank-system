import { motion } from 'framer-motion';
import {
  Wallet,
  TrendingDown,
  Target,
  BarChart3,
  ArrowUp,
  ArrowDown,
  Users,
  CreditCard,
  Activity,
  Landmark,
  Building2,
} from 'lucide-react';
import { Card } from '@/components/ui/card';
import { usePrivacy } from '@/context/privacy-context';
import { cn } from '@/lib/utils';
import { useCountUp } from '@/hooks/use-count-up';
import type { DashboardStat } from '@/hooks/use-dashboard-data';

interface StatsCardProps extends DashboardStat {
  index: number;
}

const iconMap: Record<string, React.ComponentType<{ className?: string }>> = {
  wallet: Wallet,
  'trending-down': TrendingDown,
  target: Target,
  'bar-chart-3': BarChart3,
  users: Users,
  'credit-card': CreditCard,
  activity: Activity,
  landmark: Landmark,
  'building-2': Building2,
};

export function StatsCard({
  title,
  value,
  change,
  icon,
  index,
  showChange = true,
  valueSuffix,
}: StatsCardProps) {
  const { isPrivacyMode, blurAmount } = usePrivacy();
  const displayValue = useCountUp(value, {
    duration: 1200,
    decimals: 0,
    animate: !isPrivacyMode,
  });

  const isPositive = change > 0;
  const suffix = valueSuffix ?? '';
  const Icon = iconMap[icon] || Wallet;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: index * 0.1 }}
    >
      <Card className="relative overflow-hidden group hover:shadow-2xl hover:shadow-blue-500/20 transition-all duration-300 hover:-translate-y-2 cursor-pointer border-slate-700/50 bg-slate-900/40">
        <div className="absolute inset-0 bg-gradient-to-br from-blue-500/10 to-cyan-500/10 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

        <div className="relative p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-medium text-slate-400">{title}</h3>
            <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500/20 to-cyan-500/20 flex items-center justify-center group-hover:from-blue-500/30 group-hover:to-cyan-500/30 transition-colors duration-300">
              <Icon className="h-5 w-5 text-cyan-400" />
            </div>
          </div>

          <div className="mb-4">
            <div
              className={cn('text-3xl font-bold text-white transition-all duration-300 tabular-nums')}
              style={{
                filter: isPrivacyMode ? `blur(${blurAmount}px)` : 'none',
              }}
            >
              {displayValue.toLocaleString('az-AZ')}
              {suffix ? ` ${suffix}` : ''}
            </div>
          </div>

          {showChange ? (
            <div className="flex items-center gap-2">
              <div
                className={cn(
                  'inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-semibold',
                  isPositive ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'
                )}
              >
                {isPositive ? <ArrowUp className="h-3 w-3" /> : <ArrowDown className="h-3 w-3" />}
                {Math.abs(change).toFixed(1)}%
              </div>
              <span className="text-xs text-slate-500">əvvəlki aya görə</span>
            </div>
          ) : null}
        </div>
      </Card>
    </motion.div>
  );
}
