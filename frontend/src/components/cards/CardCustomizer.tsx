import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { BankCard3D } from '@/components/cards/BankCard3D';

const GRADIENT_PRESETS = [
  { name: 'Ocean Blue', gradient: 'bg-gradient-to-br from-blue-600 to-blue-900' },
  { name: 'Purple Dream', gradient: 'bg-gradient-to-br from-purple-600 to-purple-900' },
  { name: 'Emerald', gradient: 'bg-gradient-to-br from-emerald-600 to-emerald-900' },
  { name: 'Sunset', gradient: 'bg-gradient-to-br from-orange-600 to-red-900' },
  { name: 'Midnight', gradient: 'bg-gradient-to-br from-slate-700 to-slate-900' },
  { name: 'Rose', gradient: 'bg-gradient-to-br from-rose-600 to-rose-900' },
];

const PATTERN_OVERLAYS = [
  { name: 'None', pattern: '' },
  {
    name: 'Dots',
    pattern:
      'bg-[radial-gradient(circle,rgba(255,255,255,0.2)_1px,transparent_1px)] bg-[length:20px_20px]',
  },
  {
    name: 'Lines',
    pattern:
      'bg-repeating-linear-gradient(45deg,transparent,transparent 35px,rgba(255,255,255,0.1) 35px,rgba(255,255,255,0.1) 70px)',
  },
  {
    name: 'Grid',
    pattern:
      'bg-[linear-gradient(rgba(255,255,255,0.1)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.1)_1px,transparent_1px)] bg-[length:30px_30px]',
  },
];

interface CardCustomization {
  gradient: string;
  pattern: string;
}

interface CardCustomizerProps {
  onStyleSaved?: () => void;
}

export function CardCustomizer({ onStyleSaved }: CardCustomizerProps) {
  const [customization, setCustomization] = useState<CardCustomization>({
    gradient: GRADIENT_PRESETS[0].gradient,
    pattern: '',
  });

  useEffect(() => {
    const saved = localStorage.getItem('cardCustomization');
    if (saved) {
      try {
        setCustomization(JSON.parse(saved));
      } catch {
        /* ignore */
      }
    }
  }, []);

  const handleSave = () => {
    localStorage.setItem('cardCustomization', JSON.stringify(customization));
    toast.success('Card style saved');
    onStyleSaved?.();
  };

  const handleReset = () => {
    const reset = {
      gradient: GRADIENT_PRESETS[0].gradient,
      pattern: '',
    };
    setCustomization(reset);
    localStorage.removeItem('cardCustomization');
    onStyleSaved?.();
  };

  return (
    <Card className="w-full border-slate-700 bg-slate-900/60 text-slate-100">
      <CardHeader>
        <CardTitle>Card Customizer</CardTitle>
        <CardDescription className="text-slate-400">
          Pattern applies to cards on this page after you save and refresh the list.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex justify-center">
          <div className="w-full max-w-sm">
            <BankCard3D
              cardType="debit"
              cardholderName="Your Name"
              cardNumber="4532 •••• •••• 1234"
              expiryDate="12/26"
              cvv="•••"
              gradient={customization.gradient}
              pattern={customization.pattern}
            />
          </div>
        </motion.div>

        <div className="space-y-3">
          <h3 className="font-semibold text-sm">Gradient Presets</h3>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
            {GRADIENT_PRESETS.map((preset) => (
              <motion.button
                key={preset.name}
                type="button"
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                onClick={() => setCustomization({ ...customization, gradient: preset.gradient })}
                className={`p-3 rounded-lg text-xs font-medium transition-all ${
                  customization.gradient === preset.gradient
                    ? 'ring-2 ring-offset-2 ring-offset-slate-900 ring-cyan-400'
                    : 'ring-1 ring-slate-600'
                }`}
              >
                <div className={`w-full h-16 rounded ${preset.gradient} mb-2`} />
                {preset.name}
              </motion.button>
            ))}
          </div>
        </div>

        <div className="space-y-3">
          <h3 className="font-semibold text-sm">Pattern Overlays</h3>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {PATTERN_OVERLAYS.map((pattern) => (
              <motion.button
                key={pattern.name}
                type="button"
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                onClick={() => setCustomization({ ...customization, pattern: pattern.pattern })}
                className={`p-3 rounded-lg text-xs font-medium transition-all ${
                  customization.pattern === pattern.pattern
                    ? 'ring-2 ring-offset-2 ring-offset-slate-900 ring-cyan-400'
                    : 'ring-1 ring-slate-600'
                }`}
              >
                <div
                  className={`w-full h-16 rounded bg-gradient-to-br from-slate-600 to-slate-900 ${pattern.pattern} mb-2`}
                />
                {pattern.name}
              </motion.button>
            ))}
          </div>
        </div>

        <div className="flex gap-2">
          <Button
            type="button"
            variant="outline"
            onClick={handleReset}
            className="flex-1 border-slate-600"
          >
            Reset
          </Button>
          <Button type="button" onClick={handleSave} className="flex-1">
            Save Style
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
