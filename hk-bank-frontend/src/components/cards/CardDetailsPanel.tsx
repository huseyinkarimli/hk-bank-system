import { useState } from 'react';
import { motion } from 'framer-motion';
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
  const { isPrivacyMode, blurAmount } = usePrivacy();

  const st = status.toUpperCase();
  const canBlock = st === 'ACTIVE';
  const canActivate = st === 'BLOCKED' || st === 'FROZEN';

  const maskCardNumber = (num: string) => {
    if (isPrivacyMode) return '•••• •••• •••• ••••';
    return num;
  };

  const maskIBAN = (ibanStr: string) => {
    if (isPrivacyMode) return '•••• •••• •••• •••• •••• ••••';
    return ibanStr.replace(/(\w{4})(?=\w)/g, '$1 ');
  };

  const handlePinSubmit = async () => {
    if (currentPin.length !== 4 || newPin.length !== 4) {
      toast.error('PIN must be exactly 4 digits');
      return;
    }
    setPinBusy(true);
    try {
      await onChangePin(cardId, currentPin, newPin);
      toast.success('PIN updated');
      setPinOpen(false);
      setCurrentPin('');
      setNewPin('');
    } catch (e) {
      toast.error(e instanceof Error ? e.message : 'Could not change PIN');
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
              <p className="text-sm text-slate-400">Cardholder</p>
              <p className="text-lg font-semibold text-white">{cardholderName}</p>
            </div>
            <Badge variant="outline" className={cn('shrink-0', statusBadgeClass(status))}>
              {st}
            </Badge>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-xs text-slate-400 mb-1">Card Number</p>
              <p className="text-sm font-mono font-semibold text-slate-100">
                {maskCardNumber(cardNumber)}
              </p>
            </div>
            <div>
              <p className="text-xs text-slate-400 mb-1">Card Type</p>
              <p className="text-sm font-semibold text-slate-100 capitalize">{cardType}</p>
            </div>
            <div>
              <p className="text-xs text-slate-400 mb-1">IBAN</p>
              <p className="text-xs font-mono text-slate-200 break-all">{maskIBAN(iban)}</p>
            </div>
            <div>
              <p className="text-xs text-slate-400 mb-1">Available Balance</p>
              <p
                className="text-sm font-semibold text-slate-100"
                style={
                  isPrivacyMode
                    ? { filter: `blur(${blurAmount}px)`, userSelect: 'none' }
                    : undefined
                }
              >
                {balance.toFixed(2)} AZN
              </p>
            </div>
          </div>
        </div>

        <div className="space-y-2">
          <p className="text-sm font-semibold text-white">Card Actions</p>
          <div className="grid grid-cols-2 gap-2">
            <Button
              variant="outline"
              size="sm"
              className="w-full border-slate-600 text-slate-200 hover:bg-slate-800"
              onClick={() => setPinOpen(true)}
            >
              Change PIN
            </Button>
            {canBlock ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowBlockDialog(true)}
                className="w-full text-red-400 border-red-500/40 hover:bg-red-950/40"
              >
                Block Card
              </Button>
            ) : null}
            {canActivate ? (
              <Button
                variant="outline"
                size="sm"
                onClick={async () => {
                  try {
                    await onActivateCard(cardId);
                    toast.success('Card activated');
                  } catch (e) {
                    toast.error(e instanceof Error ? e.message : 'Activation failed');
                  }
                }}
                className="w-full text-emerald-400 border-emerald-500/40 hover:bg-emerald-950/30"
              >
                Activate Card
              </Button>
            ) : null}
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowDeleteDialog(true)}
              className="w-full col-span-2 text-red-400 border-red-500/40 hover:bg-red-950/40"
            >
              Delete Card
            </Button>
          </div>
        </div>
      </motion.div>

      <Dialog open={pinOpen} onOpenChange={setPinOpen}>
        <DialogContent className="border-slate-700 bg-slate-900 text-slate-100 sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Change PIN</DialogTitle>
            <DialogDescription className="text-slate-400">
              Enter your current PIN and a new 4-digit PIN.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-2">
              <Label htmlFor="current-pin">Current PIN</Label>
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
              <Label htmlFor="new-pin">New PIN</Label>
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
              Cancel
            </Button>
            <Button onClick={() => void handlePinSubmit()} disabled={pinBusy}>
              {pinBusy ? 'Saving…' : 'Save PIN'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent className="border-slate-700 bg-slate-900 text-slate-100">
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Card</AlertDialogTitle>
            <AlertDialogDescription className="text-slate-400">
              Are you sure you want to delete this card? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <div className="text-sm bg-slate-800 p-3 rounded-lg mb-4 border border-slate-700">
            <p className="font-semibold text-slate-200">{maskCardNumber(cardNumber)}</p>
          </div>
          <div className="flex gap-2">
            <AlertDialogCancel className="border-slate-600 bg-slate-800 text-slate-200">
              Cancel
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={async () => {
                try {
                  await onDeleteCard(cardId);
                  toast.success('Card removed');
                  setShowDeleteDialog(false);
                } catch (e) {
                  toast.error(e instanceof Error ? e.message : 'Delete failed');
                }
              }}
              className="bg-red-600 hover:bg-red-700"
            >
              Delete
            </AlertDialogAction>
          </div>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={showBlockDialog} onOpenChange={setShowBlockDialog}>
        <AlertDialogContent className="border-slate-700 bg-slate-900 text-slate-100">
          <AlertDialogHeader>
            <AlertDialogTitle>Block Card</AlertDialogTitle>
            <AlertDialogDescription className="text-slate-400">
              Are you sure you want to block this card? You can activate it again later.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <div className="text-sm bg-slate-800 p-3 rounded-lg mb-4 border border-slate-700">
            <p className="font-semibold text-slate-200">{maskCardNumber(cardNumber)}</p>
          </div>
          <div className="flex gap-2">
            <AlertDialogCancel className="border-slate-600 bg-slate-800 text-slate-200">
              Cancel
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={async () => {
                try {
                  await onBlockCard(cardId);
                  toast.success('Card blocked');
                  setShowBlockDialog(false);
                } catch (e) {
                  toast.error(e instanceof Error ? e.message : 'Block failed');
                }
              }}
              className="bg-red-600 hover:bg-red-700"
            >
              Block Card
            </AlertDialogAction>
          </div>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
