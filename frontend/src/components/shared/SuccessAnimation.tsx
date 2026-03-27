import { useEffect, useId, useRef, useState } from 'react';
import { Button } from '@/components/ui/button';

interface SuccessAnimationProps {
  isVisible: boolean;
  amount: string;
  currency?: string;
  title?: string;
  subtitle?: string;
  onDismiss: () => void;
}

export function SuccessAnimation({
  isVisible,
  amount,
  currency = 'AZN',
  title = 'Əməliyyat uğurlu!',
  subtitle = 'Əməliyyat tamamlandı',
  onDismiss,
}: SuccessAnimationProps) {
  const uid = useId();
  const gradId = `hk-grad-${uid.replace(/:/g, '')}`;
  const [particles, setParticles] = useState<
    Array<{ id: number; left: number; delay: number; size: number; color: string }>
  >([]);
  const onDismissRef = useRef(onDismiss);
  onDismissRef.current = onDismiss;

  useEffect(() => {
    if (!isVisible) {
      setParticles([]);
      return;
    }
    const colors = ['#7C3AED', '#2563EB', '#10B981', '#F59E0B', '#EF4444'];
    setParticles(
      Array.from({ length: 200 }, (_, i) => ({
        id: i,
        left: Math.random() * 100,
        delay: Math.random() * 0.5,
        size: Math.random() * 8 + 4,
        color: colors[Math.floor(Math.random() * colors.length)]!,
      }))
    );
  }, [isVisible]);

  useEffect(() => {
    if (!isVisible || particles.length === 0) return;
    const timer = setTimeout(() => onDismissRef.current(), 5000);
    return () => clearTimeout(timer);
  }, [isVisible, particles.length]);

  if (!isVisible) return null;

  return (
    <div className="fixed inset-0 z-[100] pointer-events-none">
      {particles.map((particle) => (
        <div
          key={particle.id}
          className="fixed"
          style={{
            left: `${particle.left}%`,
            top: '-10px',
            width: `${particle.size}px`,
            height: `${particle.size}px`,
            backgroundColor: particle.color,
            borderRadius: '50%',
            animation: `hk-confetti-fall 3s ease-in forwards`,
            animationDelay: `${particle.delay}s`,
          }}
        />
      ))}

      <div className="fixed inset-0 flex items-center justify-center pointer-events-auto bg-black/40">
        <style>{`
          @keyframes hk-confetti-fall {
            0% { transform: translateY(0) rotateZ(0deg); opacity: 1; }
            100% { transform: translateY(100vh) rotateZ(720deg); opacity: 0; }
          }
          @keyframes hk-slide-up {
            0% { opacity: 0; transform: translateY(30px); }
            100% { opacity: 1; transform: translateY(0); }
          }
          @keyframes hk-draw-circle {
            0% { stroke-dashoffset: 166; }
            100% { stroke-dashoffset: 0; }
          }
          @keyframes hk-draw-check {
            0% { stroke-dashoffset: 48; }
            100% { stroke-dashoffset: 0; }
          }
          .hk-success-card svg circle {
            animation: hk-draw-circle 0.6s ease-out;
            stroke-dasharray: 166;
          }
          .hk-success-card svg polyline {
            animation: hk-draw-check 0.6s ease-out 0.3s both;
            stroke-dasharray: 48;
          }
        `}</style>

        <div
          className="glass-lg max-w-md w-full mx-4 p-8 flex flex-col items-center text-center space-y-6 hk-success-card border border-slate-700/50 bg-slate-900/90 text-slate-100"
          style={{ animation: 'hk-slide-up 0.6s ease-out' }}
        >
          <svg className="w-20 h-20" viewBox="0 0 100 100" fill="none">
            <circle cx="50" cy="50" r="45" stroke={`url(#${gradId})`} strokeWidth="2" fill="none" />
            <polyline
              points="30,50 45,65 70,40"
              stroke={`url(#${gradId})`}
              strokeWidth="3"
              fill="none"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <defs>
              <linearGradient id={gradId} x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stopColor="#7C3AED" />
                <stop offset="100%" stopColor="#2563EB" />
              </linearGradient>
            </defs>
          </svg>

          <div className="space-y-2">
            <h2 className="text-2xl font-bold text-white">{title}</h2>
            <p className="text-slate-400">{subtitle}</p>
          </div>

          <div className="text-4xl font-bold bg-gradient-to-r from-purple-400 to-blue-400 bg-clip-text text-transparent">
            {amount} {currency}
          </div>

          <Button
            type="button"
            onClick={() => onDismiss()}
            className="w-full mt-4 bg-gradient-to-r from-blue-500 to-cyan-400 text-white"
          >
            Əla!
          </Button>
        </div>
      </div>
    </div>
  );
}
