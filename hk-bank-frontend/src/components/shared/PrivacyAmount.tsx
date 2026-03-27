import { usePrivacy } from '@/context/privacy-context';
import { cn } from '@/lib/utils';

interface PrivacyAmountProps {
  value: string | number;
  className?: string;
  suffix?: string;
}

export function PrivacyAmount({ value, className, suffix = 'AZN' }: PrivacyAmountProps) {
  const { isPrivacyMode, blurAmount } = usePrivacy();
  const text = `${value} ${suffix}`;

  return (
    <span
      className={cn(className, isPrivacyMode && 'select-none')}
      style={
        isPrivacyMode
          ? { filter: `blur(${blurAmount}px)`, userSelect: 'none' }
          : undefined
      }
    >
      {isPrivacyMode ? `•••• ${suffix}` : text}
    </span>
  );
}
