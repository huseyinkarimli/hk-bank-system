import type { ReactNode } from 'react';
import { motion } from 'framer-motion';
import type { LucideIcon } from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePrivacy } from '@/context/privacy-context';

interface StatsCardProps {
  title: string;
  value: string | number | ReactNode;
  icon: LucideIcon;
  trend?: number;
  delay?: number;
  color?: 'blue' | 'green' | 'purple' | 'orange' | 'red' | 'cyan';
  className?: string;
  valueClassName?: string;
  /** Blur value when privacy mode is on (e.g. total bank balance). */
  sensitive?: boolean;
}

const iconBgMap = {
  blue: 'bg-blue-500/20',
  green: 'bg-green-500/20',
  purple: 'bg-purple-500/20',
  orange: 'bg-orange-500/20',
  red: 'bg-red-500/20',
  cyan: 'bg-cyan-500/20',
};

const iconTextMap = {
  blue: 'text-blue-400',
  green: 'text-green-400',
  purple: 'text-purple-400',
  orange: 'text-orange-400',
  red: 'text-red-400',
  cyan: 'text-cyan-400',
};

export function StatsCard({
  title,
  value,
  icon: Icon,
  trend,
  delay = 0,
  color = 'blue',
  className,
  valueClassName,
  sensitive = false,
}: StatsCardProps) {
  const { isPrivacyMode, blurAmount } = usePrivacy();

  const displayValue =
    sensitive && (typeof value === 'string' || typeof value === 'number') ? (
      <span
        className="inline-block transition-[filter]"
        style={{ filter: isPrivacyMode ? `blur(${blurAmount}px)` : undefined }}
      >
        {value}
      </span>
    ) : (
      value
    );

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay, duration: 0.4 }}
      className={cn('group relative', className)}
    >
      <div className="absolute inset-0 rounded-xl bg-gradient-to-r from-slate-800/50 to-slate-700/30 blur-xl opacity-0 transition duration-500 group-hover:opacity-60" />
      <div className="relative rounded-xl border border-white/10 bg-white/5 p-6 shadow-lg shadow-black/20 backdrop-blur-xl transition hover:border-white/15">
        <div className="mb-4 flex items-start justify-between">
          <div>
            <p className="mb-2 text-sm font-medium text-slate-400">{title}</p>
            <h3 className={cn('text-3xl font-bold text-white', valueClassName)}>{displayValue}</h3>
            {trend !== undefined ? (
              <p className={`mt-2 text-sm ${trend >= 0 ? 'text-emerald-400' : 'text-red-400'}`}>
                {trend >= 0 ? '↑' : '↓'} {Math.abs(trend)}% from last week
              </p>
            ) : null}
          </div>
          <div className={cn(iconBgMap[color], 'rounded-lg p-3 backdrop-blur-sm')}>
            <Icon className={cn('h-6 w-6', iconTextMap[color])} />
          </div>
        </div>
      </div>
    </motion.div>
  );
}
