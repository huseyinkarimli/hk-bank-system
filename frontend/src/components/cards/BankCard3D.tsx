import { useRef, useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Eye, EyeOff } from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePrivacy } from '@/context/privacy-context';
import { useCountUp } from '@/hooks/use-count-up';

export interface BankCard3DProps {
  cardType: 'debit' | 'credit' | 'virtual';
  cardholderName: string;
  /** Masked or fallback label when full PAN is unavailable */
  cardNumber: string;
  /** Raw 16 digits — enables show/hide full number */
  panDigits?: string;
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
  panDigits,
  expiryDate,
  cvv,
  balance,
  gradient,
  pattern,
  isExpanded = false,
}: BankCard3DProps) {
  const [isFlipped, setIsFlipped] = useState(false);
  const [showFullPan, setShowFullPan] = useState(false);
  const [rotateX, setRotateX] = useState(0);
  const [rotateY, setRotateY] = useState(0);
  const [finePointer, setFinePointer] = useState(true);
  const cardRef = useRef<HTMLDivElement>(null);
  const { isPrivacyMode, blurAmount } = usePrivacy();

  const balanceAnimated = useCountUp(balance ?? 0, {
    duration: 1200,
    decimals: 2,
    animate: !isPrivacyMode && balance !== undefined,
  });

  useEffect(() => {
    const mq = window.matchMedia('(pointer: fine)');
    const update = () => setFinePointer(mq.matches);
    update();
    mq.addEventListener('change', update);
    return () => mq.removeEventListener('change', update);
  }, []);

  const maxTilt = finePointer ? 8 : 2;

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!cardRef.current || !finePointer) return;

    const rect = cardRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const centerX = rect.width / 2;
    const centerY = rect.height / 2;

    const rotateXValue = ((y - centerY) / centerY) * maxTilt;
    const rotateYValue = ((centerX - x) / centerX) * maxTilt;

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

  const pan16 = panDigits?.replace(/\D/g, '') ?? '';
  const hasRevealablePan = pan16.length === 16;

  const displayPanLine = () => {
    if (isPrivacyMode) return '•••• •••• •••• ••••';
    if (hasRevealablePan) {
      if (showFullPan) {
        return pan16.replace(/(\d{4})(?=\d)/g, '$1 ');
      }
      return `${pan16.slice(0, 4)} **** **** ${pan16.slice(12)}`;
    }
    return maskCardNumber(cardNumber);
  };

  const balanceText =
    balance !== undefined
      ? balanceAnimated.toLocaleString('az-AZ', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      : '—';

  return (
    <div
      ref={cardRef}
      onMouseMove={handleMouseMove}
      onMouseLeave={handleMouseLeave}
      className={cn('relative', !finePointer && 'touch-manipulation')}
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
              {cardType === 'debit' && 'DEBET KART'}
              {cardType === 'credit' && 'KREDİT KART'}
              {cardType === 'virtual' && 'VİRTUAL KART'}
            </div>
            <div className="w-12 h-8 bg-yellow-400 rounded opacity-80" />
          </div>

          <div className="relative z-10 space-y-4">
            <div className="space-y-1">
              <div className="flex items-center justify-between gap-2">
                <div className="text-white/50 text-xs">Kart nömrəsi</div>
                {hasRevealablePan && !isPrivacyMode ? (
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      setShowFullPan((v) => !v);
                    }}
                    className="rounded-md p-1 text-white/70 hover:bg-white/10 hover:text-white"
                    aria-label={showFullPan ? 'Mask card number' : 'Show full card number'}
                  >
                    {showFullPan ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </button>
                ) : null}
              </div>
              <div className="text-white text-xl font-mono font-semibold tracking-wider">
                {displayPanLine()}
              </div>
            </div>

            <div className="flex justify-between items-end">
              <div className="space-y-1">
                <div className="text-white/50 text-xs">Kart sahibi</div>
                <div className="text-white font-semibold">{cardholderName}</div>
              </div>
              <div className="space-y-1">
                <div className="text-white/50 text-xs">Bitmə</div>
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
              <div className="text-white/50 text-xs">Hesab balansı (IBAN)</div>
              <div className="text-white/40 text-[10px] leading-tight mb-1">
                Kartın öz balansı yoxdur — vəsait bağlı hesabınızdadır.
              </div>
              <div
                className="text-white text-2xl font-bold tabular-nums"
                style={
                  isPrivacyMode
                    ? { filter: `blur(${blurAmount}px)`, userSelect: 'none' }
                    : undefined
                }
              >
                {balanceText} AZN
              </div>
            </div>
          )}
        </motion.div>
      </motion.div>
    </div>
  );
}
