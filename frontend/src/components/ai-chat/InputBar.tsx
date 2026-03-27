import { useState, useRef, useEffect, type KeyboardEvent, type ChangeEvent } from 'react';
import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { Send } from 'lucide-react';
import { cn } from '@/lib/utils';

interface InputBarProps {
  onSendMessage: (message: string) => void;
  isLoading: boolean;
  variant?: 'page' | 'panel';
  className?: string;
}

export function InputBar({
  onSendMessage,
  isLoading,
  variant = 'page',
  className,
}: InputBarProps) {
  const [message, setMessage] = useState('');
  const [charCount, setCharCount] = useState(0);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const isPanel = variant === 'panel';

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = Math.min(textareaRef.current.scrollHeight, 96) + 'px';
    }
  }, [message]);

  const handleInputChange = (e: ChangeEvent<HTMLTextAreaElement>) => {
    const value = e.target.value;
    setMessage(value);
    setCharCount(value.length);
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      if (message.trim() && !isLoading) {
        onSendMessage(message);
        setMessage('');
        setCharCount(0);
      }
    }
  };

  const handleSend = () => {
    if (message.trim() && !isLoading) {
      onSendMessage(message);
      setMessage('');
      setCharCount(0);
    }
  };

  return (
    <motion.div
      className={cn(
        'z-20 shrink-0 backdrop-blur-xl bg-white/10 border-t border-white/20',
        isPanel ? 'rounded-b-2xl px-3 py-2.5' : 'rounded-b-2xl px-5 py-3 md:px-6 md:py-4',
        className
      )}
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ type: 'spring', stiffness: 280, damping: 30 }}
    >
      <div className="max-w-full flex gap-2 items-end">
        <div className="flex-1 relative min-w-0">
          <textarea
            ref={textareaRef}
            value={message}
            onChange={handleInputChange}
            onKeyDown={handleKeyDown}
            placeholder="Mesajınızı yazın..."
            disabled={isLoading}
            rows={1}
            className="w-full bg-white/5 border border-white/20 rounded-lg px-3 py-2.5 text-sm text-gray-100 placeholder-gray-500 resize-none focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-500/30 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed max-h-24"
            style={{ maxHeight: '96px' }}
          />
          {charCount > 100 ? (
            <span className="absolute bottom-2 right-3 text-xs text-gray-500">{charCount}</span>
          ) : null}
        </div>

        <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
          <Button
            type="button"
            onClick={handleSend}
            disabled={!message.trim() || isLoading}
            size="icon"
            className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white border-0 rounded-lg h-10 w-10 flex-shrink-0 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 hover:shadow-lg hover:shadow-purple-500/50"
          >
            {isLoading ? (
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
                className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full"
              />
            ) : (
              <Send className="w-5 h-5" />
            )}
          </Button>
        </motion.div>
      </div>
    </motion.div>
  );
}
