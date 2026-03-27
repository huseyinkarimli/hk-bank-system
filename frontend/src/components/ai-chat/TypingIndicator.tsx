import { motion } from 'framer-motion';

export function TypingIndicator() {
  const dotVariants = {
    animate: {
      y: [0, -8, 0],
      transition: {
        duration: 0.6,
        repeat: Infinity,
      },
    },
  };

  return (
    <div className="bg-white/10 backdrop-blur-md px-4 py-2 rounded-3xl rounded-tl-sm border border-white/20 flex gap-1 items-center">
      {[0, 1, 2].map((i) => (
        <motion.div
          key={i}
          className="w-2 h-2 rounded-full bg-gray-400"
          variants={dotVariants}
          animate="animate"
          transition={{ delay: i * 0.15 }}
        />
      ))}
    </div>
  );
}
