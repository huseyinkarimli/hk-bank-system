import { motion } from 'framer-motion';
import { Card } from '@/components/ui/card';
import { usePrivacy } from '@/context/privacy-context';
import { useCountUp } from '@/hooks/use-count-up';
import { Link } from 'react-router-dom';
import type { Account } from '@/hooks/use-dashboard-data';

interface AccountsProps {
  accounts: Account[];
  isLoading: boolean;
}

function AccountRow({
  account,
  index,
}: {
  account: Account;
  index: number;
}) {
  const { isPrivacyMode, blurAmount } = usePrivacy();
  const animated = useCountUp(account.balance, {
    duration: 1200,
    decimals: 2,
    animate: !isPrivacyMode,
  });

  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: index * 0.05 }}
      className="flex items-center justify-between p-4 rounded-lg bg-slate-800/30 hover:bg-slate-800/50 transition-colors duration-200 cursor-pointer group touch-manipulation"
    >
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-white group-hover:text-cyan-400 transition-colors truncate">
          {account.name}
        </p>
        <p className="text-xs text-slate-500">{account.type}</p>
      </div>
      <div className="text-right shrink-0 pl-2">
        <div
          className="text-sm font-semibold text-white transition-all duration-300 tabular-nums"
          style={{
            filter: isPrivacyMode ? `blur(${blurAmount}px)` : 'none',
          }}
        >
          {animated.toLocaleString('az-AZ', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}{' '}
          {account.currency}
        </div>
        <p className="text-xs text-slate-500">{account.lastTransaction}</p>
      </div>
    </motion.div>
  );
}

export function Accounts({ accounts, isLoading }: AccountsProps) {
  return (
    <Card className="col-span-1 md:col-span-2 p-6 border-slate-700/50 bg-slate-900/40">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-lg font-semibold text-white">Hesablarınız</h2>
        <Link
          to="/accounts"
          className="text-sm text-cyan-400 hover:text-cyan-300 font-medium min-h-[44px] inline-flex items-center touch-manipulation"
        >
          Hamısına bax
        </Link>
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
            <AccountRow key={account.id} account={account} index={index} />
          ))}
        </div>
      )}
    </Card>
  );
}
