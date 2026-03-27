import { useEffect, useState } from 'react';

const EASE_OUT_CUBIC = (t: number) => 1 - (1 - t) ** 3;

export function useCountUp(
  endValue: number,
  options?: {
    duration?: number;
    decimals?: number;
    animate?: boolean;
  }
) {
  const duration = options?.duration ?? 1200;
  const decimals = options?.decimals ?? 0;
  const animate = options?.animate ?? true;

  const endFormatted =
    decimals > 0 ? Number(endValue.toFixed(decimals)) : Math.floor(endValue);

  const [value, setValue] = useState(() => (animate ? 0 : endFormatted));

  useEffect(() => {
    if (!animate) {
      setValue(endFormatted);
      return;
    }

    let raf = 0;
    let startTime: number | null = null;
    const from = 0;

    const tick = (now: number) => {
      if (startTime === null) startTime = now;
      const raw = Math.min((now - startTime) / duration, 1);
      const t = EASE_OUT_CUBIC(raw);
      const current = from + (endValue - from) * t;
      setValue(decimals > 0 ? Number(current.toFixed(decimals)) : Math.floor(current));
      if (raw < 1) {
        raf = requestAnimationFrame(tick);
      } else {
        setValue(endFormatted);
      }
    };

    setValue(0);
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [endValue, duration, decimals, animate, endFormatted]);

  return value;
}
