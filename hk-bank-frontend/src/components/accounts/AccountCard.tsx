import { useState } from 'react';
import { useCountUp } from '@/hooks/use-count-up';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Eye, EyeOff, Copy, Trash2, Calendar } from 'lucide-react';
import { usePrivacy } from '@/context/privacy-context';
import { toast } from 'sonner';

export interface AccountCardModel {
  id: string;
  currency: 'AZN' | 'USD' | 'EUR';
  balance: number;
  iban: string;
  status: 'ACTIVE' | 'BLOCKED' | 'CLOSED';
}

const CURRENCY_EMOJIS: Record<string, string> = {
  AZN: '🇦🇿',
  USD: '🇺🇸',
  EUR: '🇪🇺',
};

interface AccountCardProps {
  account: AccountCardModel;
  onStatement: (account: AccountCardModel) => void;
  onDelete: (accountId: string) => void;
}

export function AccountCard({ account, onStatement, onDelete }: AccountCardProps) {
  const [showFullIban, setShowFullIban] = useState(false);
  const { isPrivate } = usePrivacy();
  const balanceDisplay = useCountUp(account.balance, {
    duration: 1200,
    decimals: 2,
    animate: !isPrivate,
  });

  const maskedIban = `${account.iban.slice(0, 4)} ${account.iban.slice(4, 8)} **** **** **** **${account.iban.slice(-2)}`;
  const displayIban = showFullIban ? account.iban : maskedIban;

  const getBalanceColor = (balance: number) => {
    if (balance > 1000) return 'text-green-500';
    if (balance > 100) return 'text-yellow-500';
    return 'text-red-500';
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-500/20 text-green-700 dark:text-green-300';
      case 'BLOCKED':
        return 'bg-red-500/20 text-red-700 dark:text-red-300';
      case 'CLOSED':
        return 'bg-gray-500/20 text-gray-700 dark:text-gray-300';
      default:
        return 'bg-blue-500/20 text-blue-700 dark:text-blue-300';
    }
  };

  const copyIban = () => {
    void navigator.clipboard.writeText(account.iban);
    toast.success('IBAN kopyalandı');
  };

  return (
    <Card className="relative overflow-hidden bg-gradient-to-br from-card to-card/80 border-border/50 backdrop-blur-sm p-6 space-y-4 hover:border-accent/50 transition-colors">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <span className="text-2xl">{CURRENCY_EMOJIS[account.currency]}</span>
          <div>
            <h3 className="font-semibold text-foreground">{account.currency}</h3>
            <p className="text-xs text-muted-foreground">
              {account.currency === 'AZN'
                ? 'Azərbaycan manatı'
                : account.currency === 'USD'
                  ? 'ABŞ dolları'
                  : 'Avro'}
            </p>
          </div>
        </div>
        <Badge className={getStatusColor(account.status)}>
          {account.status === 'ACTIVE'
            ? 'Aktiv'
            : account.status === 'BLOCKED'
              ? 'Bloklanmış'
              : 'Bağlı'}
        </Badge>
      </div>

      <div className="space-y-2">
        <Label className="text-xs font-medium text-muted-foreground">IBAN</Label>
        <div className="flex items-center gap-2 bg-muted/30 rounded-lg px-3 py-2">
          <button
            type="button"
            className="flex-1 text-left font-mono text-sm text-foreground hover:text-accent-foreground transition-colors cursor-pointer min-w-0"
            onClick={() => {
              copyIban();
            }}
            title="Kopyalamaq üçün klikləyin"
          >
            <span className={showFullIban ? 'text-green-500' : undefined}>{displayIban}</span>
          </button>
          <button
            type="button"
            onClick={() => setShowFullIban(!showFullIban)}
            className="p-1 hover:bg-muted/50 rounded transition-colors shrink-0"
            aria-label={showFullIban ? 'Gizlət' : 'Göstər'}
          >
            {showFullIban ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
          <button
            type="button"
            onClick={copyIban}
            className="p-1 hover:bg-muted/50 rounded transition-colors shrink-0"
            aria-label="Kopyala"
          >
            <Copy size={16} />
          </button>
        </div>
      </div>

      <div className="space-y-2">
        <Label className="text-xs font-medium text-muted-foreground">Balans</Label>
        <div
          className={`text-3xl font-bold transition-all duration-300 tabular-nums ${isPrivate ? 'blur-sm select-none' : getBalanceColor(account.balance)}`}
        >
          {isPrivate
            ? '••••'
            : `${balanceDisplay.toLocaleString('az-AZ', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ${account.currency}`}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-2 pt-2">
        <Button
          variant="outline"
          size="sm"
          className="text-xs"
          onClick={() => onStatement(account)}
        >
          <Calendar size={14} className="mr-1" />
          Hesabat
        </Button>
        <Button
          variant="outline"
          size="sm"
          className="text-xs text-destructive hover:text-destructive"
          disabled={account.balance !== 0}
          onClick={() => onDelete(account.id)}
        >
          <Trash2 size={14} className="mr-1" />
          Sil
        </Button>
      </div>
    </Card>
  );
}
