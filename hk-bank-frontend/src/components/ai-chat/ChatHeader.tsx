import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { MessageCircle } from 'lucide-react';
import { cn } from '@/lib/utils';

interface ChatHeaderProps {
  messageCount: number;
  onNewSession: () => void;
  /** Full-width glass bar (embedded in dashboard shell) */
  variant?: 'page' | 'panel';
  className?: string;
}

export function ChatHeader({
  messageCount,
  onNewSession,
  variant = 'page',
  className,
}: ChatHeaderProps) {
  const isPanel = variant === 'panel';

  return (
    <motion.div
      className={cn(
        'z-20 shrink-0 backdrop-blur-xl bg-white/10 border-b border-white/20',
        isPanel ? 'rounded-t-2xl' : 'rounded-t-2xl',
        className
      )}
      initial={{ opacity: 0, y: -12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ type: 'spring', stiffness: 280, damping: 30 }}
    >
      <div
        className={cn(
          'flex items-center justify-between max-w-full',
          isPanel ? 'px-3 py-2.5' : 'px-5 py-3 md:px-6 md:py-4'
        )}
      >
        <div className="flex items-center gap-3 min-w-0">
          <div className="relative flex-shrink-0">
            <motion.div
              className="absolute inset-0 rounded-full border-2 border-purple-500/50"
              animate={{ scale: [1, 1.3], opacity: [1, 0] }}
              transition={{ duration: 2, repeat: Infinity }}
            />
            <div
              className={cn(
                'rounded-full bg-gradient-to-br from-purple-600 via-blue-500 to-cyan-400 flex items-center justify-center text-white',
                isPanel ? 'w-9 h-9' : 'w-12 h-12'
              )}
            >
              <MessageCircle className={isPanel ? 'w-4 h-4' : 'w-6 h-6'} />
            </div>
          </div>

          <div className="flex flex-col gap-0.5 min-w-0">
            <h2 className={cn('text-white font-semibold truncate', isPanel ? 'text-sm' : 'text-lg')}>
              HK Assistant
            </h2>
            <div className="flex items-center gap-2">
              <motion.div
                className="w-2 h-2 rounded-full bg-green-400"
                animate={{ scale: [1, 1.2, 1] }}
                transition={{ duration: 2, repeat: Infinity }}
              />
              <span className="text-xs text-green-400 font-medium">Online</span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2 sm:gap-4 flex-shrink-0">
          <div className="text-right hidden sm:block">
            <p className="text-[10px] text-gray-400 uppercase tracking-wide">Mesajlar</p>
            <p className={cn('font-semibold text-white', isPanel ? 'text-sm' : 'text-lg')}>
              {messageCount}
            </p>
          </div>
          <Button
            type="button"
            onClick={onNewSession}
            size={isPanel ? 'sm' : 'default'}
            className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white border-0 rounded-lg font-medium transition-all duration-200 hover:shadow-lg hover:shadow-purple-500/50"
          >
            Yeni söhbət
          </Button>
        </div>
      </div>
    </motion.div>
  );
}
