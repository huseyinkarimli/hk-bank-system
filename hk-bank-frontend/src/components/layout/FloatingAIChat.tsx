import { useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { useLocation } from 'react-router-dom';
import { MessageCircle, X } from 'lucide-react';
import { useAuth } from '@/context/auth-context';
import { useAIChat } from '@/context/ai-chat-context';
import { ChatHeader } from '@/components/ai-chat/ChatHeader';
import { ChatThread } from '@/components/ai-chat/ChatThread';
import { InputBar } from '@/components/ai-chat/InputBar';
import { cn } from '@/lib/utils';

const spring = { type: 'spring' as const, stiffness: 320, damping: 32 };

/**
 * Global floating AI assistant — visible on all authenticated routes except the full AI Support page.
 */
export function FloatingAIChat() {
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const { pathname } = useLocation();
  const [open, setOpen] = useState(false);
  const {
    messages,
    isSessionLoading,
    isWaitingForResponse,
    sendMessage,
    newSession,
    retryLastServiceError,
  } = useAIChat();

  if (authLoading || !isAuthenticated) return null;
  if (pathname === '/ai-support') return null;

  return (
    <div
      className={cn(
        'fixed bottom-5 right-5 z-[100] flex flex-col items-end gap-0',
        'max-md:bottom-4 max-md:right-4'
      )}
      style={{ pointerEvents: 'none' }}
    >
      <AnimatePresence mode="sync">
        {open ? (
          <motion.div
            key="ai-chat-panel"
            role="dialog"
            aria-label="HK AI Assistant"
            initial={{ opacity: 0, scale: 0.88, y: 24 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.92, y: 16 }}
            transition={spring}
            className={cn(
              'pointer-events-auto mb-3 flex flex-col overflow-hidden rounded-2xl border border-white/20',
              'bg-slate-950/92 backdrop-blur-2xl shadow-2xl shadow-purple-950/50',
              'w-[min(calc(100vw-2rem),380px)] h-[min(calc(100dvh-6.5rem),520px)]'
            )}
          >
            <ChatHeader
              variant="panel"
              messageCount={messages.length}
              onNewSession={() => {
                void newSession();
              }}
            />
            <ChatThread
              variant="panel"
              className="flex-1 min-h-0"
              messages={messages}
              isLoading={isSessionLoading}
              isWaitingForResponse={isWaitingForResponse}
              onQuickAction={(text) => void sendMessage(text)}
              onRetryServiceError={() => void retryLastServiceError()}
            />
            <InputBar
              variant="panel"
              onSendMessage={(msg) => void sendMessage(msg)}
              isLoading={isWaitingForResponse}
            />
          </motion.div>
        ) : null}
      </AnimatePresence>

      <motion.button
        type="button"
        style={{ pointerEvents: 'auto' }}
        whileHover={{ scale: 1.06 }}
        whileTap={{ scale: 0.94 }}
        transition={spring}
        onClick={() => setOpen((v) => !v)}
        className={cn(
          'relative flex h-14 w-14 items-center justify-center rounded-full',
          'bg-gradient-to-br from-purple-600 via-blue-600 to-cyan-500 text-white shadow-xl shadow-purple-600/35',
          'ring-2 ring-white/20 focus:outline-none focus-visible:ring-4 focus-visible:ring-purple-400/50'
        )}
        aria-expanded={open}
        aria-label={open ? 'AI söhbəti bağla' : 'AI köməkçini aç'}
      >
        <AnimatePresence mode="wait" initial={false}>
          {open ? (
            <motion.span
              key="x"
              initial={{ opacity: 0, rotate: -90 }}
              animate={{ opacity: 1, rotate: 0 }}
              exit={{ opacity: 0, rotate: 90 }}
              transition={{ duration: 0.2 }}
              className="absolute inset-0 flex items-center justify-center"
            >
              <X className="h-6 w-6" />
            </motion.span>
          ) : (
            <motion.span
              key="msg"
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.8 }}
              transition={{ duration: 0.2 }}
              className="absolute inset-0 flex items-center justify-center"
            >
              <MessageCircle className="h-7 w-7" />
            </motion.span>
          )}
        </AnimatePresence>
      </motion.button>
    </div>
  );
}
