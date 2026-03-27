import { useCallback, useEffect, useMemo, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { toast } from 'sonner';
import { DashboardLayout } from '@/components/dashboard/layout';
import { BankCard3D } from '@/components/cards/BankCard3D';
import { CardDetailsPanel } from '@/components/cards/CardDetailsPanel';
import { CreateCardModal } from '@/components/cards/CreateCardModal';
import { CardCustomizer } from '@/components/cards/CardCustomizer';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { EmptyState } from '@/components/empty-state';
import { useAuth } from '@/context/auth-context';
import { useCards } from '@/hooks/use-cards';

function lastFourFromPan(display: string): string {
  const digits = display.replace(/\D/g, '');
  if (digits.length >= 4) return digits.slice(-4);
  return '0000';
}

function CardsContent() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const {
    cards,
    accounts,
    isLoading,
    error,
    refetch,
    createCard,
    updateCardStatus,
    deleteCard,
    changePin,
  } = useCards();

  const [selectedCardId, setSelectedCardId] = useState<string | undefined>();
  const [showCreateModal, setShowCreateModal] = useState(false);

  const selectedCard = useMemo(
    () => cards.find((c) => c.id === selectedCardId),
    [cards, selectedCardId]
  );

  const cardholderPreview =
    [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.email || 'Kart sahibi';

  const handleCreate = useCallback(
    async (data: { cardType: 'debit' | 'credit' | 'virtual'; accountId: string }) => {
      try {
        await createCard(data.accountId, data.cardType);
        toast.success('Kart yaradıldı');
      } catch (e) {
        toast.error(e instanceof Error ? e.message : 'Kart yaradıla bilmədi');
        throw e;
      }
    },
    [createCard]
  );

  useEffect(() => {
    if (cards.length === 0) {
      setSelectedCardId(undefined);
      return;
    }
    if (!selectedCardId || !cards.some((c) => c.id === selectedCardId)) {
      setSelectedCardId(cards[0].id);
    }
  }, [cards, selectedCardId]);

  return (
    <DashboardLayout isAdmin={isAdmin}>
      {error ? (
        <Alert variant="destructive" className="mb-6 border-red-500/40 bg-red-950/30">
          <AlertTitle>Xəta baş verdi</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      ) : null}

      <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <motion.div
          initial={{ opacity: 0, y: -12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35 }}
        >
          <h1 className="text-3xl md:text-4xl font-bold text-white mb-2">Kartlarım</h1>
          <p className="text-slate-400 text-sm md:text-base">
            Ödəniş kartlarınızı idarə edin və fərdiləşdirin
          </p>
        </motion.div>
        <Button
          onClick={() => setShowCreateModal(true)}
          className="shrink-0 bg-gradient-to-r from-blue-500 to-cyan-400 text-white shadow-lg shadow-blue-500/25"
          disabled={accounts.length === 0 || isLoading}
        >
          + Yeni kart
        </Button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-4">
          <h2 className="text-lg font-semibold text-white">Kartlarınız</h2>

          {isLoading ? (
            <p className="text-slate-400 text-sm">Kartlar yüklənir…</p>
          ) : cards.length === 0 ? (
            <EmptyState
              className="border-slate-700/50 bg-slate-900/40"
              icon="💳"
              title="Hələ kart yoxdur"
              description={
                accounts.length === 0
                  ? 'Kart yaratmaq üçün əvvəlcə «Hesablar» bölməsindən hesab açın.'
                  : 'Yeni kart yaratmaq üçün düyməyə toxunun.'
              }
              action={
                accounts.length > 0
                  ? { label: 'Yeni kart', onClick: () => setShowCreateModal(true) }
                  : undefined
              }
            />
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <AnimatePresence>
                {cards.map((card, index) => (
                  <motion.button
                    key={card.id}
                    type="button"
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -20 }}
                    transition={{ delay: index * 0.08 }}
                    onClick={() => setSelectedCardId(card.id)}
                    className={`rounded-lg p-4 border-2 transition-all cursor-pointer text-left group ${
                      selectedCardId === card.id
                        ? 'border-cyan-400/80 bg-cyan-500/10'
                        : 'border-slate-700 hover:border-cyan-500/40 bg-slate-900/40'
                    }`}
                  >
                    <div className="space-y-3">
                      <BankCard3D
                        cardType={card.cardType}
                        cardholderName={card.cardholderName}
                        cardNumber={card.displayCardNumber}
                        expiryDate={card.expiryLabel}
                        cvv="•••"
                        balance={card.balance}
                        gradient={card.gradient}
                        pattern={card.pattern}
                        isExpanded
                      />
                      <div className="text-sm">
                        <p className="font-semibold text-slate-100 capitalize">
                          {card.cardType === 'debit'
                            ? 'Debet kart'
                            : card.cardType === 'credit'
                              ? 'Kredit kart'
                              : 'Virtual kart'}
                        </p>
                        <p className="text-xs text-slate-500">
                          Son 4: •••• {lastFourFromPan(card.displayCardNumber)}
                        </p>
                      </div>
                    </div>
                  </motion.button>
                ))}
              </AnimatePresence>
            </div>
          )}

          <div className="mt-8">
            <CardCustomizer onStyleSaved={() => void refetch()} />
          </div>
        </div>

        <div className="lg:col-span-1">
          <Card className="sticky top-24 border-slate-700 bg-slate-900/60 text-slate-100">
            <div className="p-6">
              <h2 className="text-lg font-semibold mb-4 text-white">Kart təfərrüatları</h2>
              {selectedCard ? (
                <CardDetailsPanel
                  cardId={selectedCard.id}
                  cardholderName={selectedCard.cardholderName}
                  cardNumber={selectedCard.displayCardNumber}
                  cardType={selectedCard.cardType}
                  status={selectedCard.status}
                  iban={selectedCard.iban}
                  balance={selectedCard.balance}
                  onBlockCard={(id) => updateCardStatus(id, 'BLOCKED')}
                  onActivateCard={(id) => updateCardStatus(id, 'ACTIVE')}
                  onDeleteCard={(id) => deleteCard(id)}
                  onChangePin={(id, currentPin, newPin) => changePin(id, currentPin, newPin)}
                />
              ) : (
                <p className="text-sm text-slate-400">Kart seçilməyib</p>
              )}
            </div>
          </Card>
        </div>
      </div>

      <CreateCardModal
        open={showCreateModal}
        onOpenChange={setShowCreateModal}
        cardholderPreviewName={cardholderPreview}
        accounts={accounts}
        onCreateCard={(data) => handleCreate(data)}
      />
    </DashboardLayout>
  );
}

export default function CardsPage() {
  return <CardsContent />;
}
