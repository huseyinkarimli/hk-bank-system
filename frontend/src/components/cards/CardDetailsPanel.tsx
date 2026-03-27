import { useState } from 'react';
import { motion } from 'framer-motion';
import { Eye, EyeOff } from 'lucide-react';
import { useCountUp } from '@/hooks/use-count-up';
import { toast } from 'sonner';
import { usePrivacy } from '@/context/privacy-context';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { cn } from '@/lib/utils';

export interface CardDetailsPanelProps {
  cardId: string;
  cardholderName: string;
  cardNumber: string;
  panDigits?: string;
  cvvPlain?: string | null;
  cardType: 'debit' | 'credit' | 'virtual';
  status: string;
  iban: string;
  balance: number;
  onBlockCard: (cardId: string) => Promise<void>;
  onActivateCard: (cardId: string) => Promise<void>;
  onDeleteCard: (cardId: string) => Promise<void>;
  onChangePin: (cardId: string, currentPin: string, newPin: string) => Promise<void>;
}

function statusBadgeClass(status: string): string {
  const u = status.toUpperCase();
  if (u === 'ACTIVE') {
    return 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30';
  }
  if (u === 'BLOCKED') {
    return 'bg-red-500/15 text-red-300 border-red-500/30';
  }
  if (u === 'FROZEN') {
    return 'bg-amber-500/15 text-amber-300 border-amber-500/30';
  }
  return 'bg-slate-500/15 text-slate-300 border-slate-500/30';
}

export function CardDetailsPanel({
  cardId,
  cardholderName,
  cardNumber,
  panDigits,
  cvvPlain,
  cardType,
  status,
  iban,
  balance,
  onBlockCard,
  onActivateCard,
  onDeleteCard,
  onChangePin,
}: CardDetailsPanelProps) {
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showBlockDialog, setShowBlockDialog] = useState(false);
  const [pinOpen, setPinOpen] = useState(false);
  const [currentPin, setCurrentPin] = useState('');
  const [newPin, setNewPin] = useState('');
  const [pinBusy, setPinBusy] = useState(false);
  const [showFullPan, setShowFullPan] = useState(false);
  const [showCvv, setShowCvv] = useState(false);
  const { isPrivacyMode, blurAmount } = usePrivacy();
  const balanceAnimated = useCountUp(balance, {
    duration: 1200,
    decimals: 2,
    animate: !isPrivacyMode,
  });

  const st = status.toUpperCase();
  const canBlock = st === 'ACTIVE';
  const canActivate = st === 'BLOCKED' || st === 'FROZEN';

  const pan16 = panDigits?.replace(/\D/g, '') ?? '';
  const hasRevealablePan = pan16.length === 16;
  const hasCvv = !!(cvvPlain && /^\d{3,4}$/.test(cvvPlain));

  const maskCardNumber = (num: string) => {
    if (isPrivacyMode) return '•••• •••• •••• ••••';
    return num;
  };

  const displayPan = () => {
    if (isPrivacyMode) return '•••• •••• •••• ••••';
    if (hasRevealablePan) {
      if (showFullPan) return pan16.replace(/(\d{4})(?=\d)/g, '$1 ');
      return `${pan16.slice(0, 4)} **** **** ${pan16.slice(12)}`;
    }
    return maskCardNumber(cardNumber);
  };

  const maskIBAN = (ibanStr: string) => {
    if (isPrivacyMode) return '•••• •••• •••• •••• •••• ••••';
    return ibanStr.replace(/(\w{4})(?=\w)/g, '$1 ');
  };

  const handlePinSubmit = async () => {
    if (currentPin.length !== 4 || newPin.length !== 4) {
      toast.error('PIN dəqiq 4 rəqəm olmalıdır');
      return;
    }
    setPinBusy(true);
    try {
      await onChangePin(cardId, currentPin, newPin);
      toast.success('PIN yeniləndi');
      setPinOpen(false);
      setCurrentPin('');
      setNewPin('');
    } catch (e) {
      toast.error(e instanceof Error ? e.message : 'PIN dəyişdirilə bilmədi');
    } finally {
      setPinBusy(false);
    }
  };

  return (
    <>
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -10 }}
        className="space-y-6"
      >
        <div className="space-y-4">
          <div className="flex items-start justify-between gap-2">
            <div>
              <p className="text-sm text-slate-400">Kart sahibi</p>
              <p className="text-lg font-semibold text-white">{cardholderName}</p>
            </div>
            <Badge variant="outline" className={cn('shrink-0', statusBadgeClass(status))}>
              {st}
            </Badge>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <div className="flex items-center justify-between gap-1 mb-1">
                <p className="text-xs text-slate-400">Kart nömrəsi</p>
                {hasRevealablePan && !isPrivacyMode ? (
                  <button
                    type="button"
                    className="text-slate-400 hover:text-white p-0.5 rounded"
                    onClick={() => setShowFullPan((v) => !v)}
                    aria-label="Toggle full card number"
                  >
                    {showFullPan ? <EyeOff className="h-3.5 w-3.5" /> : <Eye className="h-3.5 w-3.5" />}
                  </button>
                ) : null}
              </div>
              <p className="text-sm font-mono font-semibold text-slate-100">{displayPan()}</p>
            </div>
            <div>
              <p className="text-xs text-slate-400 mb-1">Kart növü</p>
              <p className="text-sm font-semibold text-slate-100 capitalize">{cardType}</p>
            </div>
            <div>
              <p className="text-xs text-slate-400 mb-1">IBAN (hesab)</p>
              <p className="text-xs font-mono text-slate-200 break-all">{maskIBAN(iban)}</p>
              <p className="text-[10px] text-slate-500 mt-1 leading-snug">
                Balans hesabda (IBAN) saxlanılır; kart yalnız bu hesaba bağlıdır.
              </p>
            </div>
            <div>
              <p className="text-xs text-slate-400 mb-1">Hesab balansı</p>
              <p
                className="text-sm font-semibold text-slate-100 tabular-nums"
                style={
                  isPrivacyMode
                    ? { filter: `blur(${blurAmount}px)`, userSelect: 'none' }
                    : undefined
                }
              >
                {balanceAnimated.toLocaleString('az-AZ', {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2,
                })}{' '}
                AZN
              </p>
            </div>
            <div className="col-span-2">
              <div className="flex items-center justify-between gap-2 mb-1">
                <p className="text-xs text-slate-400">CVV</p>
                {hasCvv && !isPrivacyMode ? (
                  <button
                    type="button"
                    className="text-slate-400 hover:text-white p-0.5 rounded"
                    onClick={() => setShowCvv((v) => !v)}
                    aria-label="Toggle CVV"
                  >
                    {showCvv ? <EyeOff className="h-3.5 w-3.5" /> : <Eye className="h-3.5 w-3.5" />}
                  </button>
                ) : null}
              </div>
              <p className="text-sm font-mono font-semibold text-slate-100">
                {isPrivacyMode ? '•••' : hasCvv ? (showCvv ? cvvPlain : '•••') : '— (köhnə kartlar)'}
              </p>
            </div>
          </div>
        </div>

        <div className="space-y-2">
          <p className="text-sm font-semibold text-white">Kart əməliyyatları</p>
          <div className="grid grid-cols-2 gap-2">
            <Button
              variant="outline"
              size="sm"
              className="w-full border-slate-600 text-slate-200 hover:bg-slate-800"
              onClick={() => setPinOpen(true)}
            >
              PIN dəyiş
            </Button>
            {canBlock ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowBlockDialog(true)}
                className="w-full text-red-400 border-red-500/40 hover:bg-red-950/40"
              >
                Kartı blokla
              </Button>
            ) : null}
            {canActivate ? (
              <Button
                variant="outline"
                size="sm"
                onClick={async () => {
                  try {
                    await onActivateCard(cardId);
                    toast.success('Kart aktivləşdirildi');
                  } catch (e) {
                    toast.error(e instanceof Error ? e.message : 'Aktivləşdirmə alınmadı');
                  }
                }}
                className="w-full text-emerald-400 border-emerald-500/40 hover:bg-emerald-950/30"
              >
                Kartı aktivləşdir
              </Button>
            ) : null}
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowDeleteDialog(true)}
              className="w-full col-span-2 text-red-400 border-red-500/40 hover:bg-red-950/40"
            >
              Kartı sil
            </Button>
          </div>
        </div>
      </motion.div>

      <Dialog open={pinOpen} onOpenChange={setPinOpen}>
        <DialogContent className="border-slate-700 bg-slate-900 text-slate-100 sm:max-w-md">
          <DialogHeader>
            <DialogTitle>PIN dəyişdirilməsi</DialogTitle>
            <DialogDescription className="text-slate-400">
              Cari PIN və yeni 4 rəqəmli PIN daxil edin. Yeni kart üçün ilkin PIN adətən 0000 və ya kart
              yaradarkən təyin etdiyiniz dəyərdir.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-2">
              <Label htmlFor="current-pin">Cari PIN</Label>
              <Input
                id="current-pin"
                type="password"
                inputMode="numeric"
                maxLength={4}
                className="font-mono text-center text-lg bg-slate-800 border-slate-600"
                value={currentPin}
                onChange={(e) => setCurrentPin(e.target.value.replace(/\D/g, '').slice(0, 4))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="new-pin">Yeni PIN</Label>
              <Input
                id="new-pin"
                type="password"
                inputMode="numeric"
                maxLength={4}
                className="font-mono text-center text-lg bg-slate-800 border-slate-600"
                value={newPin}
                onChange={(e) => setNewPin(e.target.value.replace(/\D/g, '').slice(0, 4))}
              />
            </div>
          </div>
          <DialogFooter className="gap-2 sm:gap-0">
            <Button variant="outline" onClick={() => setPinOpen(false)} className="border-slate-600">
              İmtina
            </Button>
            <Button onClick={() => void handlePinSubmit()} disabled={pinBusy}>
              {pinBusy ? 'Yadda saxlanır…' : 'PIN saxla'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent className="border-slate-700 bg-slate-900 text-slate-100">
          <AlertDialogHeader>
            <AlertDialogTitle>Kartı silmək</AlertDialogTitle>
            <AlertDialogDescription className="text-slate-400">
              Bu kartı silmək istədiyinizə əminsiniz? Bu əməliyyat geri alına bilməz.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <div className="text-sm bg-slate-800 p-3 rounded-lg mb-4 border border-slate-700">
            <p className="font-semibold text-slate-200">{maskCardNumber(cardNumber)}</p>
          </div>
          <div className="flex gap-2">
            <AlertDialogCancel className="border-slate-600 bg-slate-800 text-slate-200">
              İmtina
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={async () => {
                try {
                  await onDeleteCard(cardId);
                  toast.success('Kart silindi');
                  setShowDeleteDialog(false);
                } catch (e) {
                  toast.error(e instanceof Error ? e.message : 'Silinmə alınmadı');
                }
              }}
              className="bg-red-600 hover:bg-red-700"
            >
              Sil
            </AlertDialogAction>
          </div>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={showBlockDialog} onOpenChange={setShowBlockDialog}>
        <AlertDialogContent className="border-slate-700 bg-slate-900 text-slate-100">
          <AlertDialogHeader>
            <AlertDialogTitle>Kartı bloklamaq</AlertDialogTitle>
            <AlertDialogDescription className="text-slate-400">
              Kartı bloklamaq istədiyinizə əminsiniz? Sonradan yenidən aktivləşdirə bilərsiniz.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <div className="text-sm bg-slate-800 p-3 rounded-lg mb-4 border border-slate-700">
            <p className="font-semibold text-slate-200">{maskCardNumber(cardNumber)}</p>
          </div>
          <div className="flex gap-2">
            <AlertDialogCancel className="border-slate-600 bg-slate-800 text-slate-200">
              İmtina
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={async () => {
                try {
                  await onBlockCard(cardId);
                  toast.success('Kart bloklandı');
                  setShowBlockDialog(false);
                } catch (e) {
                  toast.error(e instanceof Error ? e.message : 'Bloklama alınmadı');
                }
              }}
              className="bg-red-600 hover:bg-red-700"
            >
              Blokla
            </AlertDialogAction>
          </div>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
