import { PrivacyAmount } from '@/components/shared/PrivacyAmount';
import { EmptyState } from '@/components/empty-state';

export interface TransferRow {
  id: string;
  label: string;
  timeLabel: string;
  amount: number;
  currency: string;
  isInbound: boolean;
}

interface TransferRecentListProps {
  rows: TransferRow[];
}

export function TransferRecentList({ rows }: TransferRecentListProps) {
  return (
    <div className="mb-6">
      <h3 className="text-xl font-bold text-white mb-4">Son əməliyyatlar</h3>
      {rows.length === 0 ? (
        <EmptyState
          className="border-slate-700/40 bg-slate-900/30 py-10"
          icon="💸"
          title="Əməliyyat yoxdur"
          description="Köçürmələr tamamlandıqca burada görünəcək."
        />
      ) : (
        <div className="space-y-3">
          {rows.map((row) => (
            <div
              key={row.id}
              className="glass-sm p-4 flex items-center justify-between border border-slate-700/40 touch-manipulation min-h-[56px]"
            >
              <div className="flex items-center gap-3 min-w-0">
                <div className="w-10 h-10 rounded-full bg-cyan-500/20 flex items-center justify-center shrink-0">
                  <div className="w-6 h-6 rounded-full bg-cyan-400/40" />
                </div>
                <div className="min-w-0">
                  <p className="font-medium text-slate-100 truncate">{row.label}</p>
                  <p className="text-xs text-slate-500">{row.timeLabel}</p>
                </div>
              </div>
              <div
                className={`font-semibold shrink-0 ml-2 flex items-baseline gap-0.5 tabular-nums ${
                  row.isInbound ? 'text-emerald-400' : 'text-slate-200'
                }`}
              >
                <span>{row.isInbound ? '+' : '−'}</span>
                <PrivacyAmount
                  value={Math.abs(row.amount).toFixed(2)}
                  suffix={row.currency}
                  className="inline font-semibold"
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
