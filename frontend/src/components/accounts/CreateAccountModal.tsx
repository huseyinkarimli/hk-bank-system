import { useEffect, useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Plus } from 'lucide-react';

const CURRENCIES = [
  { code: 'AZN', emoji: '🇦🇿', name: 'Azərbaycan manatı' },
  { code: 'USD', emoji: '🇺🇸', name: 'ABŞ dolları' },
  { code: 'EUR', emoji: '🇪🇺', name: 'Avro' },
] as const;

interface CreateAccountModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (currency: string) => void;
  existingCurrencies: string[];
}

export function CreateAccountModal({
  isOpen,
  onClose,
  onConfirm,
  existingCurrencies,
}: CreateAccountModalProps) {
  const [selectedCurrency, setSelectedCurrency] = useState<string | null>(null);

  useEffect(() => {
    if (!isOpen) setSelectedCurrency(null);
  }, [isOpen]);

  const handleConfirm = () => {
    if (selectedCurrency) {
      onConfirm(selectedCurrency);
      setSelectedCurrency(null);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Yeni hesab aç</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Hər valyutadan yalnız bir hesab aça bilərsiniz.
          </p>
          <div className="grid gap-3">
            {CURRENCIES.map((currency) => {
              const isSelected = selectedCurrency === currency.code;
              const isDisabled = existingCurrencies.includes(currency.code);

              return (
                <Card
                  key={currency.code}
                  onClick={() => !isDisabled && setSelectedCurrency(currency.code)}
                  className={`p-4 cursor-pointer transition-all ${
                    isDisabled
                      ? 'opacity-50 cursor-not-allowed bg-muted'
                      : isSelected
                        ? 'ring-2 ring-accent bg-accent/10'
                        : 'hover:border-accent/50'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <span className="text-3xl">{currency.emoji}</span>
                      <div>
                        <p className="font-semibold">{currency.code}</p>
                        <p className="text-sm text-muted-foreground">{currency.name}</p>
                      </div>
                    </div>
                    {isDisabled ? (
                      <Badge variant="outline">Artıq mövcuddur</Badge>
                    ) : null}
                  </div>
                </Card>
              );
            })}
          </div>
          <div className="flex gap-2 pt-4">
            <Button variant="outline" onClick={onClose} className="flex-1">
              İmtina
            </Button>
            <Button onClick={handleConfirm} disabled={!selectedCurrency} className="flex-1">
              <Plus size={16} className="mr-2" />
              Hesab aç
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
