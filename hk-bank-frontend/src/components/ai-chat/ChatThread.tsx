import { useEffect, useRef } from 'react';
import { motion } from 'framer-motion';
import { parseISO } from 'date-fns';
import { MessageCircle } from 'lucide-react';
import { Skeleton } from '@/components/ui/skeleton';
import { QuickActions } from '@/components/ai-chat/QuickActions';
import { MessageBubble, TypingIndicatorRow } from '@/components/ai-chat/MessageBubble';
import type { ChatMessage } from '@/context/ai-chat-context';
import { cn } from '@/lib/utils';

interface ChatThreadProps {
  messages: ChatMessage[];
  isLoading: boolean;
  isWaitingForResponse: boolean;
  onQuickAction: (text: string) => void;
  onRetryServiceError?: () => void;
  variant?: 'page' | 'panel';
  className?: string;
}

function groupMessagesByDate(messages: ChatMessage[]) {
  const groups: Record<string, ChatMessage[]> = {};
  const today = new Date().toDateString();

  messages.forEach((msg) => {
    let dateKey = 'Bu gün';
    if (msg.timestamp) {
      try {
        const msgDate = parseISO(msg.timestamp).toDateString();
        if (msgDate !== today) {
          const yesterday = new Date();
          yesterday.setDate(yesterday.getDate() - 1);
          dateKey = msgDate === yesterday.toDateString() ? 'Dünən' : msgDate;
        }
      } catch {
        dateKey = 'Bu gün';
      }
    }

    if (!groups[dateKey]) groups[dateKey] = [];
    groups[dateKey].push(msg);
  });

  return groups;
}

export function ChatThread({
  messages,
  isLoading,
  isWaitingForResponse,
  onQuickAction,
  onRetryServiceError,
  variant = 'page',
  className,
}: ChatThreadProps) {
  const scrollRef = useRef<HTMLDivElement>(null);
  const isPanel = variant === 'panel';

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTo({
        top: scrollRef.current.scrollHeight,
        behavior: 'smooth',
      });
    }
  }, [messages, isWaitingForResponse]);

  const isEmpty = messages.length === 0 && !isLoading;
  const groupedMessages = groupMessagesByDate(messages);

  if (isLoading) {
    return (
      <div
        className={cn(
          'flex-1 overflow-y-auto custom-scrollbar px-4 py-3',
          isPanel ? 'min-h-[200px]' : 'min-h-[320px]',
          className
        )}
      >
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <motion.div
              key={i}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: i * 0.1 }}
              className={`flex ${i % 2 === 0 ? 'justify-end' : 'justify-start'}`}
            >
              <Skeleton className="w-56 h-12 rounded-lg bg-white/10" />
            </motion.div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div
      ref={scrollRef}
      className={cn(
        'flex-1 overflow-y-auto custom-scrollbar ai-chat-mesh-bg px-4 py-3 min-h-0',
        isPanel ? 'min-h-[200px]' : 'min-h-[320px]',
        className
      )}
    >
      {isEmpty ? (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="flex flex-col items-center justify-center h-full min-h-[240px] gap-6 py-6"
        >
          <motion.div
            animate={{ scale: [1, 1.05, 1] }}
            transition={{ duration: 2, repeat: Infinity }}
            className="w-16 h-16 sm:w-20 sm:h-20 rounded-full bg-gradient-to-br from-purple-600 via-blue-500 to-cyan-400 flex items-center justify-center text-white shadow-lg shadow-purple-500/50"
          >
            <MessageCircle className="w-8 h-8 sm:w-10 sm:h-10" />
          </motion.div>

          <div className="text-center max-w-md px-2">
            <p className="text-gray-200 text-base sm:text-lg leading-relaxed">
              Salam! Mən HK Bank-ın AI köməkçisiyəm. Sizə necə kömək edə bilərəm?
            </p>
          </div>

          <QuickActions onActionClick={onQuickAction} />
        </motion.div>
      ) : (
        <div className="space-y-3 pb-2">
          {Object.entries(groupedMessages).map(([dateKey, msgs]) => (
            <div key={dateKey}>
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                className="flex justify-center my-3"
              >
                <span className="text-xs text-gray-500 bg-white/5 px-3 py-1 rounded-full border border-white/10">
                  {dateKey}
                </span>
              </motion.div>

              {msgs.map((msg, idx) => {
                const useTypewriter =
                  msg.role === 'assistant' && !msg.errorKind && Boolean(msg.animateReply);

                return (
                  <div
                    key={`${dateKey}-${idx}-${msg.id ?? ''}-${msg.timestamp ?? ''}-${msg.content.slice(0, 12)}`}
                    className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'} mb-3`}
                  >
                    <motion.div
                      initial={{
                        opacity: 0,
                        x: msg.role === 'user' ? 20 : -20,
                      }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ duration: 0.4 }}
                      className="max-w-[min(100%,24rem)]"
                    >
                      <MessageBubble
                        message={msg}
                        useTypewriter={useTypewriter}
                        onRetry={
                          msg.errorKind === 'service_unavailable' ? onRetryServiceError : undefined
                        }
                      />
                    </motion.div>
                  </div>
                );
              })}
            </div>
          ))}

          {isWaitingForResponse ? <TypingIndicatorRow /> : null}
        </div>
      )}
    </div>
  );
}
