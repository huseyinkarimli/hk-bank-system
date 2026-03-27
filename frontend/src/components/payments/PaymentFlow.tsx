import { useEffect, useMemo, useState } from 'react';
import { toast } from 'sonner';
import { AlertCircle, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { PrivacyAmount } from '@/components/shared/PrivacyAmount';
import { SuccessAnimation } from '@/components/shared/SuccessAnimation';
import { usePaymentsPageData, type ProviderRow } from '@/hooks/use-payments-data';
import { apiClient } from '@/lib/axios-client';

const CATEGORY_COLORS: Record<string, string> = {
  MOBILE: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
  INTERNET: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
  ELECTRICITY: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
  WATER: 'bg-cyan-500/20 text-cyan-400 border-cyan-500/30',
  GAS: 'bg-red-500/20 text-red-400 border-red-500/30',
  TV: 'bg-purple-500/20 text-purple-400 border-purple-500/30',
  OTHER: 'bg-slate-500/20 text-slate-400 border-slate-500/30',
};

const CATEGORY_LABELS: Record<string, string> = {
  MOBILE: 'Mobil',
  INTERNET: 'İnternet',
  ELECTRICITY: 'Elektrik',
  WATER: 'Su',
  GAS: 'Qaz',
  TV: 'TV',
  OTHER: 'Digər',
};

function providerEmoji(type: string): string {
  const t = type.toUpperCase();
  if (t === 'MOBILE') return '📱';
  if (t === 'INTERNET') return '🌐';
  if (t === 'ELECTRICITY') return '⚡';
  if (t === 'WATER') return '💧';
  if (t === 'GAS') return '🔥';
  if (t === 'TV') return '📺';
  return '💳';
}

function formatPayTime(iso: string): string {
  try {
    return new Date(iso).toLocaleString('az-AZ', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  } catch {
    return iso;
  }
}

function formatPhoneDisplay(input: string) {
  const cleaned = input.replace(/\D/g, '').slice(0, 10);
  if (cleaned.length <= 2) return cleaned;
  if (cleaned.length <= 5) return `${cleaned.slice(0, 2)} ${cleaned.slice(2)}`;
  return `${cleaned.slice(0, 2)} ${cleaned.slice(2, 5)} ${cleaned.slice(5)}`;
}

export function PaymentFlow() {
  const { accounts, providers, payments, loading, refetch } = usePaymentsPageData();
  const [selected, setSelected] = useState<ProviderRow | null>(null);
  const [accountId, setAccountId] = useState('');
  const [subscriberNumber, setSubscriberNumber] = useState('');
  const [amount, setAmount] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [successVisible, setSuccessVisible] = useState(false);
  const [successAmount, setSuccessAmount] = useState('');

  const sourceAccount = useMemo(
    () => accounts.find((a) => a.id === accountId) ?? accounts[0],
    [accounts, accountId]
  );

  useEffect(() => {
    if (accounts.length > 0 && !accountId) {
      setAccountId(accounts[0]!.id);
    }
  }, [accounts, accountId]);

  const handleSelect = (row: ProviderRow) => {
    setSelected(row);
    setSubscriberNumber('');
    setAmount('');
    setError(null);
  };

  const handleSubmit = async () => {
    if (!selected || !sourceAccount) return;
    const rawSub =
      selected.providerType.toUpperCase() === 'MOBILE'
        ? subscriberNumber.replace(/\D/g, '')
        : subscriberNumber.trim();
    if (!rawSub || !amount) {
      setError('Bütün sahələri doldurun');
      toast.error('Bütün sahələri doldurun');
      return;
    }
    if (selected.providerType.toUpperCase() === 'MOBILE' && rawSub.length < 10) {
      setError('Düzgün telefon nömrəsi daxil edin');
      toast.error('Düzgün telefon nömrəsi daxil edin');
      return;
    }
    const amt = parseFloat(amount.replace(',', '.'));
    if (!Number.isFinite(amt) || amt <= 0 || amt > 500) {
      setError('Məbləğ 0-dan böyük və ən çox 500 AZN olmalıdır');
      toast.error('Məbləğ 0-dan böyük və ən çox 500 AZN olmalıdır');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      await apiClient.post('/api/payments', {
        accountId: Number(sourceAccount.id),
        providerType: selected.providerType,
        providerName: selected.providerName,
        subscriberNumber: rawSub,
        amount: amt,
      });
      setSuccessAmount(amt.toFixed(2));
      setSuccessVisible(true);
      setSelected(null);
      setSubscriberNumber('');
      setAmount('');
      toast.success('Ödəniş uğurla tamamlandı');
      void refetch();
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Ödəniş uğursuz oldu';
      setError(msg);
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading && accounts.length === 0 && providers.length === 0) {
    return <p className="text-slate-400 text-sm">Yüklənir…</p>;
  }

  return (
    <div className="max-w-6xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-white mb-2">Ödənişlər</h1>
        <p className="text-slate-400">Kommunal xidmətlər və mobil operatorlar</p>
      </div>

      {!selected ? (
        <>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 mb-8">
            {providers.length === 0 ? (
              <p className="col-span-full text-slate-500 text-sm">
                Provayder siyahısı boşdur və ya yüklənmədi
              </p>
            ) : (
              providers.map((p, idx) => (
                <button
                  key={`${p.providerType}-${p.providerName}-${idx}`}
                  type="button"
                  onClick={() => handleSelect(p)}
                  className="glass-lg p-4 group hover:bg-slate-800/40 transition-all active:scale-95 text-left border border-slate-700/40 rounded-2xl"
                >
                  <div className="text-4xl mb-3 group-hover:scale-110 transition-transform">
                    {providerEmoji(p.providerType)}
                  </div>
                  <h3 className="font-semibold text-slate-100 text-sm mb-2 line-clamp-2">
                    {p.providerName}
                  </h3>
                  <span
                    className={`inline-block text-xs px-2 py-1 rounded-full border ${
                      CATEGORY_COLORS[p.providerType.toUpperCase()] ?? CATEGORY_COLORS.OTHER
                    }`}
                  >
                    {CATEGORY_LABELS[p.providerType.toUpperCase()] ?? p.providerType}
                  </span>
                </button>
              ))
            )}
          </div>

          <div>
            <h3 className="text-xl font-bold text-white mb-4">Son ödənişlər</h3>
            {payments.length === 0 ? (
              <p className="text-sm text-slate-500">Hələ ödəniş yoxdur</p>
            ) : (
              <div className="space-y-3">
                {payments.slice(0, 8).map((pay) => (
                  <div
                    key={pay.id}
                    className="glass-sm p-4 flex items-center justify-between border border-slate-700/40 rounded-lg"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-10 h-10 rounded-full bg-cyan-500/20 flex items-center justify-center text-xl shrink-0">
                        {providerEmoji(pay.providerType)}
                      </div>
                      <div className="min-w-0">
                        <p className="font-medium text-slate-100 truncate">{pay.providerName}</p>
                        <p className="text-xs text-slate-500">
                          {formatPayTime(pay.createdAt)} · {pay.status}
                        </p>
                      </div>
                    </div>
                    <div className="font-semibold text-slate-200 shrink-0 ml-2 flex items-baseline gap-0.5">
                      <span>−</span>
                      <PrivacyAmount
                        value={
                          typeof pay.amount === 'number'
                            ? pay.amount.toFixed(2)
                            : parseFloat(String(pay.amount)).toFixed(2)
                        }
                        suffix="AZN"
                        className="inline font-semibold"
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      ) : (
        <div className="max-w-md mx-auto">
          <div className="glass-lg p-6 space-y-6 border border-slate-700/40 rounded-2xl">
            <div className="flex items-center justify-between gap-2">
              <div className="flex items-center gap-3 min-w-0">
                <div className="text-4xl shrink-0">{providerEmoji(selected.providerType)}</div>
                <div className="min-w-0">
                  <h2 className="text-xl font-bold text-slate-100 truncate">
                    {selected.providerName}
                  </h2>
                  <p className="text-xs text-slate-500">
                    {CATEGORY_LABELS[selected.providerType.toUpperCase()] ??
                      selected.providerType}
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => {
                  setSelected(null);
                  setError(null);
                }}
                className="p-2 hover:bg-slate-800 rounded-full transition-all shrink-0"
              >
                <X size={20} className="text-slate-400" />
              </button>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-200">Hesab</label>
              <select
                value={accountId || accounts[0]?.id}
                onChange={(e) => setAccountId(e.target.value)}
                className="glass w-full px-4 py-3 text-slate-100 bg-slate-900/60 border border-slate-700/50 outline-none rounded-lg"
              >
                {accounts.map((acc) => (
                  <option key={acc.id} value={acc.id} className="bg-slate-900">
                    {acc.label} — {acc.balance.toFixed(2)} {acc.currency}
                  </option>
                ))}
              </select>
              {sourceAccount ? (
                <p className="text-sm text-slate-500">
                  Balans:{' '}
                  <PrivacyAmount
                    value={sourceAccount.balance.toFixed(2)}
                    suffix={sourceAccount.currency}
                    className="text-slate-300 font-medium"
                  />
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-200">
                {selected.providerType.toUpperCase() === 'MOBILE'
                  ? 'Telefon nömrəsi'
                  : 'Abonent nömrəsi'}
              </label>
              <Input
                value={subscriberNumber}
                onChange={(e) => {
                  if (selected.providerType.toUpperCase() === 'MOBILE') {
                    setSubscriberNumber(formatPhoneDisplay(e.target.value));
                  } else {
                    setSubscriberNumber(e.target.value);
                  }
                }}
                placeholder={
                  selected.providerType.toUpperCase() === 'MOBILE' ? '50 123 45 67' : '1234567890'
                }
                className="glass bg-slate-900/40 border-slate-700 text-slate-100"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-200">Məbləğ</label>
              <div className="flex gap-2">
                <Input
                  value={amount}
                  onChange={(e) => setAmount(e.target.value.replace(/[^\d.,]/g, '').replace(',', '.'))}
                  placeholder="0.00"
                  type="text"
                  inputMode="decimal"
                  className="glass text-2xl font-bold text-center bg-slate-900/40 border-slate-700 text-slate-100"
                />
                <div className="glass px-4 flex items-center text-lg font-semibold text-slate-200 border border-slate-700/50 rounded-lg">
                  AZN
                </div>
              </div>
              <p className="text-xs text-slate-500">Maksimum: 500 AZN</p>
            </div>

            {error ? (
              <div className="glass-sm bg-red-950/40 border border-red-500/30 p-3 flex gap-2 rounded-lg">
                <AlertCircle size={20} className="text-red-400 shrink-0" />
                <p className="text-red-300 text-sm">{error}</p>
              </div>
            ) : null}

            {accounts.length === 0 ? (
              <p className="text-amber-400 text-sm">Ödəniş üçün hesab tapılmadı.</p>
            ) : null}

            <div className="flex gap-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => {
                  setSelected(null);
                  setError(null);
                }}
                disabled={submitting}
                className="flex-1 border-slate-600 text-slate-200"
              >
                Ləğv et
              </Button>
              <Button
                type="button"
                onClick={() => void handleSubmit()}
                disabled={
                  !subscriberNumber || !amount || submitting || accounts.length === 0
                }
                className="flex-1 bg-gradient-to-r from-blue-500 to-cyan-400 text-white"
              >
                {submitting ? 'Ödənilir…' : 'Ödə'}
              </Button>
            </div>
          </div>
        </div>
      )}

      <SuccessAnimation
        isVisible={successVisible}
        amount={successAmount}
        currency="AZN"
        subtitle="Ödəniş tamamlandı"
        onDismiss={() => setSuccessVisible(false)}
      />
    </div>
  );
}
