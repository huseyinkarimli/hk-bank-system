import { motion } from 'framer-motion';
import { Card } from '@/components/ui/card';
import { usePrivacy } from '@/context/privacy-context';
import type { Account } from '@/hooks/use-dashboard-data';

interface AccountsProps {
  accounts: Account[];
  isLoading: boolean;
}

export function Accounts({ accounts, isLoading }: AccountsProps) {
  const { isPrivacyMode, blurAmount } = usePrivacy();

  return (
    <Card className="col-span-1 md:col-span-2 p-6 border-slate-700/50 bg-slate-900/40">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-lg font-semibold text-white">Your Accounts</h2>
        <button
          type="button"
          className="text-sm text-cyan-400 hover:text-cyan-300 font-medium"
        >
          View All
        </button>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="h-16 bg-slate-800/50 rounded-lg animate-pulse" />
          ))}
        </div>
      ) : (
        <div className="space-y-3 max-h-80 overflow-y-auto scrollbar-hide">
          {accounts.map((account, index) => (
            <motion.div
              key={account.id}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.05 }}
              className="flex items-center justify-between p-4 rounded-lg bg-slate-800/30 hover:bg-slate-800/50 transition-colors duration-200 cursor-pointer group"
            >
              <div className="flex-1">
                <p className="text-sm font-medium text-white group-hover:text-cyan-400 transition-colors">
                  {account.name}
                </p>
                <p className="text-xs text-slate-500">{account.type}</p>
              </div>
              <div className="text-right">
                <div
                  className="text-sm font-semibold text-white transition-all duration-300"
                  style={{
                    filter: isPrivacyMode ? `blur(${blurAmount}px)` : 'none',
                  }}
                >
                  {account.balance.toLocaleString('en-HK')} {account.currency}
                </div>
                <p className="text-xs text-slate-500">{account.lastTransaction}</p>
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </Card>
  );
}
