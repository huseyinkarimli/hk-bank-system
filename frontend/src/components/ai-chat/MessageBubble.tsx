import { motion } from 'framer-motion';
import { formatRelative, parseISO } from 'date-fns';
import { az } from 'date-fns/locale';
import { MessageCircle, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { usePrivacy } from '@/context/privacy-context';
import type { ChatMessage } from '@/context/ai-chat-context';
import { TypingIndicator } from '@/components/ai-chat/TypingIndicator';

function getAzerbaijaniDate(timestamp?: string) {
  if (!timestamp) return '';
  try {
    const dateObj = parseISO(timestamp);
    return formatRelative(dateObj, new Date(), { locale: az });
  } catch {
    return '';
  }
}

/** Per-character reveal — preserve premium feel from original chat-area */
function TypewriterText({ text }: { text: string }) {
  return (
    <>
      {text.split('').map((char, idx) => (
        <motion.span
          key={idx}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: idx * 0.02 }}
        >
          {char}
        </motion.span>
      ))}
    </>
  );
}

interface MessageBubbleProps {
  message: ChatMessage;
  onRetry?: () => void;
  /** When true, assistant text uses typewriter (skip for error rows) */
  useTypewriter?: boolean;
}

export function MessageBubble({ message, onRetry, useTypewriter }: MessageBubbleProps) {
  const { isPrivacyMode } = usePrivacy();
  const { role, content, timestamp, errorKind } = message;

  const maybeSensitiveClass =
    isPrivacyMode && /(\d[\d\s,.]*\s*(AZN|USD|HKD|EUR|₼))|((AZN|USD|HKD|EUR|₼)\s*[\d,.]+)/i.test(
      content
    )
      ? 'blur-sm select-none'
      : '';

  if (role === 'user') {
    return (
      <div className="flex flex-col items-end gap-1">
        <div
          className={`bg-gradient-to-br from-purple-600 to-blue-600 text-white px-4 py-2 rounded-3xl rounded-br-sm shadow-lg ${maybeSensitiveClass}`}
        >
          {content}
        </div>
        {timestamp ? (
          <span className="text-xs text-gray-500 mt-1">{getAzerbaijaniDate(timestamp)}</span>
        ) : null}
      </div>
    );
  }

  if (errorKind === 'service_unavailable') {
    return (
      <div className="flex gap-3 items-start">
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-amber-500/90 via-orange-600/90 to-red-600/90 flex items-center justify-center text-white flex-shrink-0 mt-1 shadow-lg shadow-amber-500/20">
          <MessageCircle className="w-4 h-4" />
        </div>
        <div className="flex flex-col gap-2 max-w-[min(100%,20rem)]">
          <div className="bg-amber-500/10 backdrop-blur-md text-amber-50 px-4 py-3 rounded-3xl rounded-tl-sm border border-amber-400/30">
            <p className="text-sm leading-relaxed">{content}</p>
            <p className="text-xs text-amber-200/80 mt-2">Xidmət müvəqqəti əlçatan deyil (503).</p>
          </div>
          {onRetry ? (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={onRetry}
              className="self-start gap-2 border-amber-400/40 bg-white/5 text-amber-100 hover:bg-amber-500/20 hover:text-white"
            >
              <RefreshCw className="w-4 h-4" />
              Yenidən cəhd et
            </Button>
          ) : null}
          {timestamp ? (
            <span className="text-xs text-gray-500 ml-1">{getAzerbaijaniDate(timestamp)}</span>
          ) : null}
        </div>
      </div>
    );
  }

  return (
    <div className="flex gap-3 items-start">
      <div className="w-8 h-8 rounded-full bg-gradient-to-br from-purple-600 via-blue-500 to-cyan-400 flex items-center justify-center text-white flex-shrink-0 mt-1">
        <MessageCircle className="w-4 h-4" />
      </div>
      <div className="flex flex-col gap-1 min-w-0">
        <div className="bg-white/10 backdrop-blur-md text-gray-100 px-4 py-2 rounded-3xl rounded-tl-sm border border-white/20">
          {useTypewriter ? <TypewriterText text={content} /> : <span>{content}</span>}
        </div>
        {timestamp ? (
          <span className="text-xs text-gray-500 ml-2">{getAzerbaijaniDate(timestamp)}</span>
        ) : null}
      </div>
    </div>
  );
}

export function TypingIndicatorRow() {
  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex justify-start mb-4">
      <div className="flex gap-3 items-center">
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-purple-600 via-blue-500 to-cyan-400 flex items-center justify-center text-white flex-shrink-0">
          <MessageCircle className="w-4 h-4" />
        </div>
        <TypingIndicator />
      </div>
    </motion.div>
  );
}
