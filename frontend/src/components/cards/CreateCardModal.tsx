import { useState } from 'react';
import { motion } from 'framer-motion';
import { BankCard3D } from '@/components/cards/BankCard3D';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Field, FieldLabel } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import type { UICardType } from '@/hooks/use-cards';

export interface AccountSelectOption {
  id: string;
  label: string;
}

interface CreateCardModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  cardholderPreviewName: string;
  accounts: AccountSelectOption[];
  onCreateCard: (cardData: {
    cardType: UICardType;
    accountId: string;
    initialPin: string;
  }) => Promise<void>;
}

const CARD_GRADIENTS = [
  'bg-gradient-to-br from-blue-600 to-blue-900',
  'bg-gradient-to-br from-purple-600 to-purple-900',
  'bg-gradient-to-br from-indigo-600 to-indigo-900',
  'bg-gradient-to-br from-slate-600 to-slate-900',
];

export function CreateCardModal({
  open,
  onOpenChange,
  cardholderPreviewName,
  accounts,
  onCreateCard,
}: CreateCardModalProps) {
  const [cardType, setCardType] = useState<UICardType>('debit');
  const [accountId, setAccountId] = useState('');
  const [initialPin, setInitialPin] = useState('0000');
  const [selectedGradient, setSelectedGradient] = useState(CARD_GRADIENTS[0]);
  const [busy, setBusy] = useState(false);

  const handleCreate = async () => {
    if (!accountId) {
      return;
    }

    const pin = initialPin.replace(/\D/g, '').slice(0, 4);
    if (pin.length !== 4) {
      return;
    }

    setBusy(true);
    try {
      await onCreateCard({
        cardType,
        accountId,
        initialPin: pin,
      });
      setCardType('debit');
      setAccountId('');
      setInitialPin('0000');
      setSelectedGradient(CARD_GRADIENTS[0]);
      onOpenChange(false);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg border-slate-700 bg-slate-900 text-slate-100">
        <DialogHeader>
          <DialogTitle>Create New Card</DialogTitle>
          <DialogDescription className="text-slate-400">
            Kart seçdiyiniz hesaba (IBAN) bağlanır; pul hesabda saxlanılır, kart isə həmin balansdan
            xərcləyir. 4 rəqəmli PIN təyin edin (susmaya görə 0000).
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6">
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex justify-center"
          >
            <div className="w-80">
              <BankCard3D
                cardType={cardType}
                cardholderName={cardholderPreviewName}
                cardNumber="4532 •••• •••• 1234"
                expiryDate="12/26"
                cvv="•••"
                gradient={selectedGradient}
              />
            </div>
          </motion.div>

          <div className="space-y-4">
            <Field>
              <FieldLabel>Card Type</FieldLabel>
              <Select
                value={cardType}
                onValueChange={(value: UICardType) => setCardType(value)}
              >
                <SelectTrigger className="bg-slate-800 border-slate-600">
                  <SelectValue placeholder="Select card type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="debit">Debit Card</SelectItem>
                  <SelectItem value="credit">Credit Card</SelectItem>
                  <SelectItem value="virtual">Virtual Card</SelectItem>
                </SelectContent>
              </Select>
            </Field>

            <Field>
              <FieldLabel>İlkin PIN (4 rəqəm)</FieldLabel>
              <Input
                inputMode="numeric"
                maxLength={4}
                className="bg-slate-800 border-slate-600 font-mono text-center text-lg tracking-widest"
                value={initialPin}
                onChange={(e) => setInitialPin(e.target.value.replace(/\D/g, '').slice(0, 4))}
                placeholder="0000"
              />
            </Field>

            <Field>
              <FieldLabel>Linked Account</FieldLabel>
              <Select value={accountId || undefined} onValueChange={setAccountId}>
                <SelectTrigger className="bg-slate-800 border-slate-600">
                  <SelectValue placeholder="Select account" />
                </SelectTrigger>
                <SelectContent>
                  {accounts.map((a) => (
                    <SelectItem key={a.id} value={a.id}>
                      {a.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Field>

            <Field>
              <FieldLabel>Card Color (preview)</FieldLabel>
              <div className="grid grid-cols-4 gap-3">
                {CARD_GRADIENTS.map((gradient) => (
                  <motion.button
                    key={gradient}
                    type="button"
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    onClick={() => setSelectedGradient(gradient)}
                    className={`w-full h-20 rounded-lg cursor-pointer transition-all ${gradient} ${
                      selectedGradient === gradient
                        ? 'ring-2 ring-offset-2 ring-offset-slate-900 ring-cyan-400'
                        : 'ring-1 ring-slate-600'
                    }`}
                  />
                ))}
              </div>
            </Field>
          </div>

          <div className="flex gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              className="flex-1 border-slate-600"
              disabled={busy}
            >
              Cancel
            </Button>
            <Button
              type="button"
              onClick={() => void handleCreate()}
              className="flex-1"
              disabled={
                busy || !accountId || accounts.length === 0 || initialPin.replace(/\D/g, '').length !== 4
              }
            >
              {busy ? 'Creating…' : 'Create Card'}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
