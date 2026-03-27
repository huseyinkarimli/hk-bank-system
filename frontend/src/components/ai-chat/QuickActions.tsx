import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';

const QUICK_ACTIONS = [
  { id: 1, text: '💳 Kart bloklamaq istəyirəm' },
  { id: 2, text: '💸 Transfer haqqında' },
  { id: 3, text: '📊 Hesab məlumatları' },
  { id: 4, text: '🔒 Təhlükəsizlik' },
];

interface QuickActionsProps {
  onActionClick: (action: string) => void;
}

export function QuickActions({ onActionClick }: QuickActionsProps) {
  return (
    <div className="grid grid-cols-2 gap-3 w-full max-w-md">
      {QUICK_ACTIONS.map((action, idx) => (
        <motion.div
          key={action.id}
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: idx * 0.1 }}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
        >
          <Button
            type="button"
            onClick={() => onActionClick(action.text)}
            className="w-full bg-white/10 hover:bg-white/20 text-gray-100 border border-white/30 rounded-lg py-3 px-3 text-sm font-medium transition-all duration-200 hover:shadow-lg hover:shadow-purple-500/25 hover:border-purple-400/50"
          >
            {action.text}
          </Button>
        </motion.div>
      ))}
    </div>
  );
}
