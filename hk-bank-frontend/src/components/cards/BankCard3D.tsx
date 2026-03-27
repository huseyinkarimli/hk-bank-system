import { useRef, useState } from 'react';
import { motion } from 'framer-motion';
import { cn } from '@/lib/utils';
import { usePrivacy } from '@/context/privacy-context';

export interface BankCard3DProps {
  cardType: 'debit' | 'credit' | 'virtual';
  cardholderName: string;
  cardNumber: string;
  expiryDate: string;
  cvv: string;
  balance?: number;
  gradient: string;
  pattern?: string;
  isExpanded?: boolean;
}

export function BankCard3D({
  cardType,
  cardholderName,
  cardNumber,
  expiryDate,
  cvv,
  balance,
  gradient,
  pattern,
  isExpanded = false,
}: BankCard3DProps) {
  const [isFlipped, setIsFlipped] = useState(false);
  const [rotateX, setRotateX] = useState(0);
  const [rotateY, setRotateY] = useState(0);
  const cardRef = useRef<HTMLDivElement>(null);
  const { isPrivacyMode, blurAmount } = usePrivacy();

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!cardRef.current) return;

    const rect = cardRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const centerX = rect.width / 2;
    const centerY = rect.height / 2;

    const rotateXValue = ((y - centerY) / centerY) * 10;
    const rotateYValue = ((centerX - x) / centerX) * 10;

    setRotateX(rotateXValue);
    setRotateY(rotateYValue);
  };

  const handleMouseLeave = () => {
    setRotateX(0);
    setRotateY(0);
  };

  const maskCardNumber = (num: string) => {
    if (isPrivacyMode) return '•••• •••• •••• ••••';
    const cleaned = num.replace(/\s/g, '');
    if (!/^\d+$/.test(cleaned)) return num;
    return cleaned.replace(/(\d{4})(?=\d)/g, '$1 ');
  };

  const balanceText =
    balance !== undefined ? `${balance.toFixed(2)}` : '—';

  return (
    <div
      ref={cardRef}
      onMouseMove={handleMouseMove}
      onMouseLeave={handleMouseLeave}
      className="relative"
      style={{ perspective: '1000px' }}
    >
      <motion.div
        onClick={() => setIsFlipped(!isFlipped)}
        initial={{ rotateY: 0 }}
        animate={{ rotateY: isFlipped ? 180 : 0 }}
        transition={{ duration: 0.6 }}
        style={{
          rotateX,
          rotateY,
          transformStyle: 'preserve-3d',
        }}
        className="relative w-full cursor-pointer"
      >
        <motion.div
          style={{ backfaceVisibility: 'hidden' }}
          className={cn(
            'w-full h-60 rounded-2xl p-6 flex flex-col justify-between relative overflow-hidden',
            gradient
          )}
        >
          {pattern ? <div className={cn('absolute inset-0 opacity-20', pattern)} /> : null}

          <motion.div
            className="pointer-events-none absolute inset-0 bg-gradient-to-r from-transparent via-white to-transparent opacity-0"
            animate={{
              opacity: [0, 0.28, 0],
              x: ['-100%', '100%'],
            }}
            transition={{
              duration: 3,
              repeat: Infinity,
              ease: 'easeInOut',
            }}
          />

          <div className="relative z-10 space-y-2">
            <div className="text-white/70 text-xs font-semibold tracking-widest">
              {cardType === 'debit' && 'DEBIT CARD'}
              {cardType === 'credit' && 'CREDIT CARD'}
              {cardType === 'virtual' && 'VIRTUAL CARD'}
            </div>
            <div className="w-12 h-8 bg-yellow-400 rounded opacity-80" />
          </div>

          <div className="relative z-10 space-y-4">
            <div className="space-y-1">
              <div className="text-white/50 text-xs">Card Number</div>
              <div className="text-white text-xl font-mono font-semibold tracking-wider">
                {maskCardNumber(cardNumber)}
              </div>
            </div>

            <div className="flex justify-between items-end">
              <div className="space-y-1">
                <div className="text-white/50 text-xs">Card Holder</div>
                <div className="text-white font-semibold">{cardholderName}</div>
              </div>
              <div className="space-y-1">
                <div className="text-white/50 text-xs">Expires</div>
                <div
                  className={cn(
                    'text-white font-mono',
                    isPrivacyMode && 'select-none'
                  )}
                  style={
                    isPrivacyMode
                      ? { filter: `blur(${blurAmount}px)` }
                      : undefined
                  }
                >
                  {isPrivacyMode ? '••/••' : expiryDate}
                </div>
              </div>
            </div>
          </div>
        </motion.div>

        <motion.div
          style={{
            backfaceVisibility: 'hidden',
            rotateY: 180,
          }}
          className={cn(
            'absolute inset-0 w-full h-60 rounded-2xl p-6 flex flex-col justify-between',
            gradient,
            'bg-opacity-100'
          )}
        >
          <div className="relative z-10 space-y-4">
            <div className="w-full h-12 bg-black/30 rounded" />
            <div className="space-y-2">
              <div className="text-white/50 text-xs">CVV</div>
              <div
                className={cn(
                  'text-white text-3xl font-mono font-bold tracking-widest',
                  isPrivacyMode && 'select-none'
                )}
                style={
                  isPrivacyMode
                    ? { filter: `blur(${blurAmount}px)` }
                    : undefined
                }
              >
                {isPrivacyMode ? '•••' : cvv}
              </div>
            </div>
          </div>

          {isExpanded && balance !== undefined && (
            <div className="relative z-10">
              <div className="text-white/50 text-xs">Available Balance</div>
              <div
                className="text-white text-2xl font-bold"
                style={
                  isPrivacyMode
                    ? { filter: `blur(${blurAmount}px)`, userSelect: 'none' }
                    : undefined
                }
              >
                ${balanceText}
              </div>
            </div>
          )}
        </motion.div>
      </motion.div>
    </div>
  );
}
