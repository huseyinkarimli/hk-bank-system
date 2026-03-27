import { useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import confetti from 'canvas-confetti';

interface SuccessAnimationProps {
  isVisible: boolean;
  onComplete?: () => void;
  title?: string;
  message?: string;
  /** Fire confetti burst (e.g. after deposit / withdraw). */
  celebrate?: boolean;
}

export function SuccessAnimation({
  isVisible,
  onComplete,
  title = 'Success!',
  message = 'Operation completed successfully.',
  celebrate = false,
}: SuccessAnimationProps) {
  useEffect(() => {
    if (!isVisible) return;
    if (celebrate) {
      const t = window.setTimeout(() => {
        confetti({
          particleCount: 140,
          spread: 72,
          origin: { y: 0.65 },
          colors: ['#38bdf8', '#22d3ee', '#818cf8', '#34d399', '#fbbf24'],
        });
      }, 80);
      return () => window.clearTimeout(t);
    }
  }, [isVisible, celebrate]);

  useEffect(() => {
    if (!isVisible) return;
    const timer = window.setTimeout(() => onComplete?.(), 2400);
    return () => window.clearTimeout(timer);
  }, [isVisible, onComplete]);

  return (
    <AnimatePresence>
      {isVisible ? (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="pointer-events-none fixed inset-0 z-[100] flex items-center justify-center"
        >
          <motion.div
            className="rounded-xl border border-white/10 bg-slate-900/95 p-8 text-center shadow-2xl shadow-cyan-500/10 backdrop-blur-xl"
            initial={{ y: -16, scale: 0.92 }}
            animate={{ y: 0, scale: 1 }}
            transition={{ type: 'spring', stiffness: 320, damping: 24 }}
          >
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ delay: 0.15, type: 'spring', stiffness: 260 }}
              className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-emerald-500/20"
            >
              <svg className="h-9 w-9 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <motion.path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  initial={{ pathLength: 0 }}
                  animate={{ pathLength: 1 }}
                  transition={{ delay: 0.35, duration: 0.45 }}
                />
              </svg>
            </motion.div>
            <h3 className="mb-1 text-lg font-semibold text-white">{title}</h3>
            <p className="text-sm text-slate-400">{message}</p>
          </motion.div>
        </motion.div>
      ) : null}
    </AnimatePresence>
  );
}
