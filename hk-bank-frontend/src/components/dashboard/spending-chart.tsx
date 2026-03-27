import { motion } from 'framer-motion';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { Card } from '@/components/ui/card';
import type { ChartDataPoint } from '@/hooks/use-dashboard-data';
import { usePrivacy } from '@/context/privacy-context';

interface SpendingChartProps {
  data: ChartDataPoint[];
  isLoading: boolean;
}

export function SpendingChart({ data, isLoading }: SpendingChartProps) {
  const { isPrivacyMode, blurAmount } = usePrivacy();

  return (
    <Card className="col-span-1 md:col-span-3 p-6 border-slate-700/50 bg-slate-900/40">
      <div className="mb-6">
        <h2 className="text-lg font-semibold text-white">Weekly Spending</h2>
        <p className="text-sm text-slate-400">Last 7 days (outflow)</p>
      </div>

      {isLoading ? (
        <div className="h-80 bg-slate-800/50 rounded-lg animate-pulse" />
      ) : (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          style={{
            filter: isPrivacyMode ? `blur(${blurAmount}px)` : 'none',
          }}
        >
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={data}>
              <defs>
                <linearGradient id="colorSpending" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#06b6d4" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#06b6d4" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#475569" />
              <XAxis dataKey="day" stroke="#94a3b8" />
              <YAxis stroke="#94a3b8" />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#1e293b',
                  border: '1px solid #475569',
                  borderRadius: '8px',
                }}
                labelStyle={{ color: '#cbd5e1' }}
              />
              <Area
                type="monotone"
                dataKey="spending"
                stroke="#06b6d4"
                strokeWidth={2}
                fillOpacity={1}
                fill="url(#colorSpending)"
              />
            </AreaChart>
          </ResponsiveContainer>
        </motion.div>
      )}
    </Card>
  );
}
