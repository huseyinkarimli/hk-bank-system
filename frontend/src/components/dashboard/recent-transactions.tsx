import { motion } from 'framer-motion';
import { Card } from '@/components/ui/card';
import type { Transaction } from '@/hooks/use-dashboard-data';
import { usePrivacy } from '@/context/privacy-context';
import { Skeleton } from '@/components/ui/skeleton';
import { ArrowDownRight, ArrowUpLeft } from 'lucide-react';
import { cn } from '@/lib/utils';
import { useCountUp } from '@/hooks/use-count-up';
import { EmptyState } from '@/components/empty-state';

interface RecentTransactionsProps {
  transactions: Transaction[];
  isLoading: boolean;
}

function TransactionAmount({
  amount,
  currency,
  isDebit,
}: {
  amount: number;
  currency: string;
  isDebit: boolean;
}) {
  const { isPrivacyMode, blurAmount } = usePrivacy();
  const n = Math.abs(amount);
  const display = useCountUp(n, {
    duration: 1200,
    decimals: 2,
    animate: !isPrivacyMode,
  });

  return (
    <div
      className={cn(
        'text-sm font-semibold transition-all duration-300 tabular-nums',
        isDebit ? 'text-red-400' : 'text-green-400'
      )}
      style={{
        filter: isPrivacyMode ? `blur(${blurAmount}px)` : 'none',
      }}
    >
      {isDebit ? '-' : '+'}
      {display.toLocaleString('az-AZ', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}{' '}
      {currency}
    </div>
  );
}

export function RecentTransactions({ transactions, isLoading }: RecentTransactionsProps) {
  return (
    <Card className="col-span-1 md:col-span-5 p-6 border-slate-700/50 bg-slate-900/40">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-lg font-semibold text-white">Son əməliyyatlar</h2>
      </div>

      {isLoading ? (
        <div className="space-y-4">
          {[...Array(5)].map((_, i) => (
            <Skeleton key={i} className="h-12 w-full bg-slate-800/50" />
          ))}
        </div>
      ) : transactions.length === 0 ? (
        <EmptyState
          className="border-0 bg-transparent py-8"
          icon="📋"
          title="Əməliyyat yoxdur"
          description="Köçürmə və ödənişlər tamamlandıqca burada görünəcək."
        />
      ) : (
        <div className="space-y-3">
          {transactions.map((transaction, index) => (
            <motion.div
              key={transaction.id}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.05 }}
              className="flex items-center justify-between p-4 rounded-lg bg-slate-800/30 hover:bg-slate-800/50 transition-colors duration-200 touch-manipulation"
            >
              <div className="flex items-center gap-4 flex-1 min-w-0">
                <div
                  className={cn(
                    'w-10 h-10 rounded-lg flex items-center justify-center shrink-0',
                    transaction.type === 'debit' ? 'bg-red-500/20' : 'bg-green-500/20'
                  )}
                >
                  {transaction.type === 'debit' ? (
                    <ArrowDownRight className="h-5 w-5 text-red-400" />
                  ) : (
                    <ArrowUpLeft className="h-5 w-5 text-green-400" />
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-white truncate">{transaction.description}</p>
                  <div className="flex gap-2 flex-wrap">
                    <p className="text-xs text-slate-500">{transaction.category}</p>
                    <span className="text-xs text-slate-600">•</span>
                    <p className="text-xs text-slate-500">{transaction.timestamp}</p>
                  </div>
                </div>
              </div>

              <div className="text-right shrink-0 pl-2">
                <TransactionAmount
                  amount={transaction.amount}
                  currency={transaction.currency}
                  isDebit={transaction.type === 'debit'}
                />
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </Card>
  );
}
