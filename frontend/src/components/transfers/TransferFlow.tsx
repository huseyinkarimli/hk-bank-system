import { useEffect, useMemo, useState } from 'react';
import { toast } from 'sonner';
import { AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { PrivacyAmount } from '@/components/shared/PrivacyAmount';
import { SuccessAnimation } from '@/components/shared/SuccessAnimation';
import { StepIndicator } from '@/components/transfers/StepIndicator';
import { TransferRecentList, type TransferRow } from '@/components/transfers/TransferRecentList';
import { useTransferPageData } from '@/hooks/use-transfer-data';
import { apiClient } from '@/lib/axios-client';

const STEPS = [
  { number: 1, label: 'Məlumatlar' },
  { number: 2, label: 'Təsdiq' },
  { number: 3, label: 'Nəticə' },
];

function maskCardNumber(input: string) {
  const cleaned = input.replace(/\D/g, '').slice(0, 16);
  return cleaned.replace(/(\d{4})(?=\d)/g, '$1 ').trim();
}

function maskIban(input: string) {
  const cleaned = input.toUpperCase().replace(/[^A-Z0-9]/g, '');
  return cleaned.replace(/(\w{4})(?=\w)/g, '$1 ').trim();
}

function normalizeIban(s: string) {
  return s.replace(/\s/g, '').toUpperCase();
}

function parseAmount(s: string): number | null {
  const t = s.replace(',', '.').trim();
  if (!t) return null;
  const n = parseFloat(t);
  if (!Number.isFinite(n) || n < 0.01) return null;
  return n;
}

function formatTxTime(iso: string): string {
  try {
    return new Date(iso).toLocaleString('az-AZ', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  } catch {
    return iso;
  }
}

export function TransferFlow() {
  const { accounts, cardHints, transactions, loading, refetch } = useTransferPageData();
  const [currentStep, setCurrentStep] = useState(1);
  const [transferType, setTransferType] = useState<'card' | 'iban'>('card');
  const [sourceAccountId, setSourceAccountId] = useState('');
  const [sourceCardPan, setSourceCardPan] = useState('');
  const [destinationInput, setDestinationInput] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [successVisible, setSuccessVisible] = useState(false);
  const [successAmount, setSuccessAmount] = useState('');

  const sourceAccount = useMemo(
    () => accounts.find((a) => a.id === sourceAccountId) ?? accounts[0],
    [accounts, sourceAccountId]
  );

  const displayCurrency = transferType === 'iban' ? sourceAccount?.currency ?? 'AZN' : 'AZN';

  const recentRows: TransferRow[] = useMemo(() => {
    return transactions.map((tx) => {
      const inbound = tx.type === 'DEPOSIT';
      return {
        id: String(tx.id),
        label: tx.referenceNumber
          ? `${tx.type.replace(/_/g, ' ')} · ${tx.referenceNumber}`
          : tx.type.replace(/_/g, ' '),
        timeLabel: formatTxTime(tx.createdAt),
        amount: typeof tx.amount === 'number' ? tx.amount : parseFloat(String(tx.amount)) || 0,
        currency: tx.sourceCurrency ?? 'AZN',
        isInbound: inbound,
      };
    });
  }, [transactions]);

  useEffect(() => {
    if (accounts.length > 0 && !sourceAccountId) {
      setSourceAccountId(accounts[0]!.id);
    }
  }, [accounts, sourceAccountId]);

  const handleStepOne = () => {
    const amt = parseAmount(amount);
    if (transferType === 'card') {
      const src = sourceCardPan.replace(/\D/g, '');
      const dst = destinationInput.replace(/\D/g, '');
      if (src.length !== 16 || dst.length !== 16) {
        setError('Hər iki kart nömrəsi 16 rəqəm olmalıdır');
        toast.error('Hər iki kart nömrəsi 16 rəqəm olmalıdır');
        return;
      }
    } else {
      if (!sourceAccount?.iban) {
        setError('Mənbə hesab üçün IBAN tapılmadı');
        toast.error('Mənbə hesab üçün IBAN tapılmadı');
        return;
      }
      const tgt = normalizeIban(destinationInput);
      if (tgt.length < 15) {
        setError('Düzgün hədəf IBAN daxil edin');
        toast.error('Düzgün hədəf IBAN daxil edin');
        return;
      }
    }
    if (amt === null) {
      setError('Düzgün məbləğ daxil edin (minimum 0.01)');
      toast.error('Düzgün məbləğ daxil edin');
      return;
    }
    setError(null);
    setCurrentStep(2);
  };

  const executeTransfer = async () => {
    const amt = parseAmount(amount);
    if (amt === null) return;
    setSubmitting(true);
    setError(null);
    try {
      if (transferType === 'card') {
        await apiClient.post('/api/transactions/transfer/card', {
          sourceCardNumber: sourceCardPan.replace(/\D/g, ''),
          targetCardNumber: destinationInput.replace(/\D/g, ''),
          amount: amt,
          description: description.trim() || undefined,
        });
      } else {
        await apiClient.post('/api/transactions/transfer/iban', {
          sourceIban: sourceAccount!.iban,
          targetIban: normalizeIban(destinationInput),
          amount: amt,
          description: description.trim() || undefined,
        });
      }
      setSuccessAmount(amt.toFixed(2));
      setSuccessVisible(true);
      setCurrentStep(3);
      toast.success('Köçürmə uğurla tamamlandı');
      void refetch();
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Köçürmə uğursuz oldu';
      setError(msg);
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleReset = () => {
    setCurrentStep(1);
    setTransferType('card');
    setSourceCardPan('');
    setDestinationInput('');
    setAmount('');
    setDescription('');
    setError(null);
    setSuccessVisible(false);
    if (accounts[0]) setSourceAccountId(accounts[0].id);
  };

  if (loading && accounts.length === 0) {
    return <p className="text-slate-400 text-sm">Yüklənir…</p>;
  }

  return (
    <div className="max-w-6xl mx-auto">
      <div className="grid md:grid-cols-2 gap-8">
        <div>
          <StepIndicator steps={STEPS} currentStep={currentStep} />

          {currentStep === 1 && (
            <div className="glass p-6 space-y-6">
              <div className="flex gap-2 p-1 bg-slate-800/80 rounded-full border border-slate-700/50">
                <button
                  type="button"
                  onClick={() => setTransferType('card')}
                  className={`flex-1 py-2 px-4 rounded-full transition-all font-medium text-sm ${
                    transferType === 'card'
                      ? 'bg-gradient-to-r from-blue-500 to-cyan-400 text-white'
                      : 'text-slate-400'
                  }`}
                >
                  Kart üzrə
                </button>
                <button
                  type="button"
                  onClick={() => setTransferType('iban')}
                  className={`flex-1 py-2 px-4 rounded-full transition-all font-medium text-sm ${
                    transferType === 'iban'
                      ? 'bg-gradient-to-r from-blue-500 to-cyan-400 text-white'
                      : 'text-slate-400'
                  }`}
                >
                  IBAN üzrə
                </button>
              </div>

              {transferType === 'iban' && (
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-200">Mənbə hesab</label>
                  <select
                    value={sourceAccountId || accounts[0]?.id}
                    onChange={(e) => setSourceAccountId(e.target.value)}
                    className="glass w-full px-4 py-3 text-slate-100 bg-slate-900/60 border-0 outline-none rounded-lg"
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
              )}

              {transferType === 'card' && (
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-200">Mənbə kart nömrəsi</label>
                  <Input
                    value={sourceCardPan}
                    onChange={(e) => setSourceCardPan(maskCardNumber(e.target.value))}
                    placeholder="0000 0000 0000 0000"
                    className="glass bg-slate-900/40 border-slate-700 text-slate-100"
                    inputMode="numeric"
                  />
                  {cardHints.length > 0 && (
                    <p className="text-xs text-slate-500">
                      Sizin kartlarınız (yalnız xatırlatma):{' '}
                      {cardHints.map((c) => c.maskedCardNumber).join(', ')}
                    </p>
                  )}
                </div>
              )}

              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-200">
                  Məqsəd {transferType === 'card' ? 'kart' : 'IBAN'}
                </label>
                <Input
                  value={destinationInput}
                  onChange={(e) => {
                    const v =
                      transferType === 'card'
                        ? maskCardNumber(e.target.value)
                        : maskIban(e.target.value);
                    setDestinationInput(v);
                  }}
                  placeholder={
                    transferType === 'card' ? '0000 0000 0000 0000' : 'AZ00 XXXX 0000 0000 0000 0000'
                  }
                  className="glass bg-slate-900/40 border-slate-700 text-slate-100"
                />
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-200">Məbləğ</label>
                <div className="flex gap-2">
                  <Input
                    value={amount}
                    onChange={(e) =>
                      setAmount(e.target.value.replace(/[^\d.,]/g, '').replace(',', '.'))
                    }
                    placeholder="0.00"
                    type="text"
                    inputMode="decimal"
                    className="glass text-2xl font-bold text-center bg-slate-900/40 border-slate-700 text-slate-100"
                  />
                  <div className="glass px-4 flex items-center text-lg font-semibold text-slate-200 border border-slate-700/50 rounded-lg">
                    {displayCurrency}
                  </div>
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-200">Qeyd (istəyə bağlı)</label>
                <Input
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Köçürmə haqqında qeyd…"
                  className="glass bg-slate-900/40 border-slate-700 text-slate-100"
                />
              </div>

              {transferType === 'iban' && accounts.length === 0 ? (
                <p className="text-amber-400 text-sm">
                  İBAN köçürməsi üçün hesab tapılmadı.
                </p>
              ) : null}

              {error && currentStep === 1 && (
                <div className="glass-sm bg-red-950/40 border border-red-500/30 p-3 flex gap-2 rounded-lg">
                  <AlertCircle size={20} className="text-red-400 shrink-0" />
                  <p className="text-red-300 text-sm">{error}</p>
                </div>
              )}

              <Button
                type="button"
                onClick={handleStepOne}
                className="w-full bg-gradient-to-r from-blue-500 to-cyan-400 text-white"
                disabled={transferType === 'iban' && accounts.length === 0}
              >
                Davam et
              </Button>
            </div>
          )}

          {currentStep === 2 && (
            <div className="glass p-6 space-y-6">
              <div className="space-y-4">
                <div className="glass-sm p-4 border border-slate-700/40 rounded-lg">
                  <p className="text-xs text-slate-500 mb-2">Mənbə</p>
                  <p className="text-lg font-semibold text-slate-100">
                    {transferType === 'card'
                      ? maskCardNumber(sourceCardPan) || '—'
                      : sourceAccount?.iban || '—'}
                  </p>
                </div>
                <div className="flex justify-center text-slate-500">↓</div>
                <div className="glass-sm p-4 border border-slate-700/40 rounded-lg">
                  <p className="text-xs text-slate-500 mb-2">Məqsəd</p>
                  <p className="text-lg font-semibold text-slate-100 break-all">
                    {transferType === 'card' ? destinationInput : destinationInput}
                  </p>
                </div>
              </div>

              <div className="glass-sm p-4 border border-cyan-500/30 rounded-lg">
                <p className="text-xs text-slate-500 mb-1">Məbləğ</p>
                <p className="text-3xl font-bold text-slate-100">
                  {amount} {displayCurrency}
                </p>
              </div>

              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-slate-500">İcra müddəti:</span>
                  <span className="text-slate-200 font-medium">Dərhal</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Komissiya:</span>
                  <span className="text-slate-200 font-medium">0.00 AZN</span>
                </div>
              </div>

              {error && (
                <div className="glass-sm bg-red-950/40 border border-red-500/30 p-3 flex gap-2 rounded-lg">
                  <AlertCircle size={20} className="text-red-400 shrink-0" />
                  <p className="text-red-300 text-sm">{error}</p>
                </div>
              )}

              <div className="flex gap-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setError(null);
                    setCurrentStep(1);
                  }}
                  className="flex-1 border-slate-600 text-slate-200"
                  disabled={submitting}
                >
                  Geri
                </Button>
                <Button
                  type="button"
                  onClick={() => void executeTransfer()}
                  className="flex-1 bg-gradient-to-r from-blue-500 to-cyan-400 text-white"
                  disabled={submitting}
                >
                  {submitting ? 'Göndərilir…' : 'Təsdiqlə'}
                </Button>
              </div>
            </div>
          )}

          {currentStep === 3 && (
            <div className="glass p-6 space-y-4 text-center border border-slate-700/40 rounded-xl">
              <p className="text-slate-400">Köçürmə tamamlandı</p>
              <Button
                type="button"
                onClick={handleReset}
                className="w-full bg-gradient-to-r from-blue-500 to-cyan-400 text-white"
              >
                Yeni köçürmə
              </Button>
            </div>
          )}
        </div>

        <div>
          <TransferRecentList rows={recentRows} />
        </div>
      </div>

      <SuccessAnimation
        isVisible={successVisible}
        amount={successAmount}
        currency={displayCurrency}
        subtitle="Pul köçürməsi tamamlandı"
        onDismiss={() => setSuccessVisible(false)}
      />
    </div>
  );
}
