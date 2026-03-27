import { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Copy, History, ChevronDown, Calculator } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { convertCurrency, getAvailableCurrencies, formatCurrency } from '@/lib/currency';
import { playClick, playSuccess, playTransfer } from '@/lib/sounds';
import { cn } from '@/lib/utils';

interface HistoryItem {
  id: string;
  from: string;
  to: string;
  amount: number;
  result: number;
  timestamp: Date;
}

export function CurrencyCalculatorWidget() {
  const [isOpen, setIsOpen] = useState(false);
  const [amount, setAmount] = useState('100');
  const [fromCurrency, setFromCurrency] = useState('AZN');
  const [toCurrency, setToCurrency] = useState('USD');
  const [result, setResult] = useState<number>(0);
  const [history, setHistory] = useState<HistoryItem[]>([]);
  const [showHistory, setShowHistory] = useState(false);
  const widgetRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const saved = localStorage.getItem('currencyHistory');
    if (saved) {
      try {
        const parsed = JSON.parse(saved) as HistoryItem[];
        setHistory(parsed.map((item) => ({ ...item, timestamp: new Date(item.timestamp) })));
      } catch {
        /* ignore */
      }
    }
  }, []);

  useEffect(() => {
    if (amount && !Number.isNaN(Number(amount))) {
      setResult(convertCurrency(Number(amount), fromCurrency, toCurrency));
    } else {
      setResult(0);
    }
  }, [amount, fromCurrency, toCurrency]);

  const handleConvert = () => {
    if (!amount || Number.isNaN(Number(amount))) return;
    playTransfer();
    const newItem: HistoryItem = {
      id: Date.now().toString(),
      from: fromCurrency,
      to: toCurrency,
      amount: Number(amount),
      result,
      timestamp: new Date(),
    };
    const updatedHistory = [newItem, ...history].slice(0, 10);
    setHistory(updatedHistory);
    localStorage.setItem('currencyHistory', JSON.stringify(updatedHistory));
  };

  const handleSwapCurrencies = () => {
    playClick();
    setFromCurrency(toCurrency);
    setToCurrency(fromCurrency);
  };

  const handleCopyResult = () => {
    playSuccess();
    void navigator.clipboard.writeText(result.toString());
  };

  const handleClearHistory = () => {
    playClick();
    setHistory([]);
    localStorage.removeItem('currencyHistory');
  };

  useEffect(() => {
    function handlePointerDown(event: PointerEvent) {
      if (widgetRef.current && !widgetRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    if (isOpen) {
      document.addEventListener('pointerdown', handlePointerDown, true);
      return () => document.removeEventListener('pointerdown', handlePointerDown, true);
    }
  }, [isOpen]);

  return (
    <div
      ref={widgetRef}
      className="fixed bottom-28 right-4 md:right-5 z-[95] pointer-events-auto max-md:bottom-24"
    >
      <AnimatePresence>
        {isOpen ? (
          <motion.div
            initial={{ opacity: 0, scale: 0.92, y: 16 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.92, y: 16 }}
            transition={{ duration: 0.22 }}
            className="absolute bottom-[4.5rem] right-0 w-[min(calc(100vw-2rem),20rem)] rounded-2xl border border-slate-600/60 bg-slate-900/95 backdrop-blur-xl shadow-2xl p-5 mb-2"
          >
            <div className="space-y-4">
              <div className="flex items-center justify-between gap-2">
                <h3 className="text-base font-semibold text-white">Valyuta kalkulyatoru</h3>
                <button
                  type="button"
                  onClick={() => setIsOpen(false)}
                  className="p-2 min-h-[44px] min-w-[44px] flex items-center justify-center rounded-lg text-slate-400 hover:bg-slate-800 hover:text-white touch-manipulation"
                  aria-label="Bağla"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setShowHistory(false)}
                  className={cn(
                    'flex-1 min-h-[44px] py-2 px-3 rounded-lg font-medium text-sm touch-manipulation transition-colors',
                    !showHistory
                      ? 'bg-cyan-600 text-white'
                      : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                  )}
                >
                  Çevirmə
                </button>
                <button
                  type="button"
                  onClick={() => setShowHistory(true)}
                  className={cn(
                    'flex-1 min-h-[44px] py-2 px-3 rounded-lg font-medium text-sm flex items-center justify-center gap-2 touch-manipulation transition-colors',
                    showHistory
                      ? 'bg-cyan-600 text-white'
                      : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                  )}
                >
                  <History className="w-4 h-4" />
                  Tarixçə
                </button>
              </div>

              {showHistory ? (
                <div className="space-y-2 max-h-64 overflow-y-auto overscroll-contain">
                  {history.length === 0 ? (
                    <p className="text-center text-slate-500 text-sm py-6">Hələ çevirmə yoxdur</p>
                  ) : (
                    <>
                      {history.map((item) => (
                        <div
                          key={item.id}
                          className="flex items-center justify-between p-3 bg-slate-800/60 rounded-lg border border-slate-700/50"
                        >
                          <div className="flex-1 min-w-0 text-left">
                            <p className="text-sm font-medium text-slate-100 truncate">
                              {formatCurrency(item.amount, item.from)} →{' '}
                              {formatCurrency(item.result, item.to)}
                            </p>
                            <p className="text-xs text-slate-500">
                              {item.timestamp.toLocaleTimeString('az-AZ')}
                            </p>
                          </div>
                        </div>
                      ))}
                      <Button
                        type="button"
                        onClick={handleClearHistory}
                        variant="outline"
                        size="sm"
                        className="w-full mt-2 border-slate-600"
                      >
                        Tarixçəni təmizlə
                      </Button>
                    </>
                  )}
                </div>
              ) : (
                <div className="space-y-3">
                  <div>
                    <label className="block text-xs font-medium text-slate-400 mb-1">Məbləğ</label>
                    <Input
                      type="number"
                      inputMode="decimal"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                      placeholder="0"
                      className="w-full bg-slate-950/50 border-slate-600 text-white min-h-11"
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-medium text-slate-400 mb-1">Mənbə</label>
                      <select
                        value={fromCurrency}
                        onChange={(e) => setFromCurrency(e.target.value)}
                        className="w-full min-h-11 px-3 py-2 rounded-md border border-slate-600 bg-slate-950/50 text-white text-sm font-medium"
                      >
                        {getAvailableCurrencies().map((curr) => (
                          <option key={curr} value={curr}>
                            {curr}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div>
                      <label className="block text-xs font-medium text-slate-400 mb-1">Hədəf</label>
                      <select
                        value={toCurrency}
                        onChange={(e) => setToCurrency(e.target.value)}
                        className="w-full min-h-11 px-3 py-2 rounded-md border border-slate-600 bg-slate-950/50 text-white text-sm font-medium"
                      >
                        {getAvailableCurrencies().map((curr) => (
                          <option key={curr} value={curr}>
                            {curr}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                  <div className="flex justify-center">
                    <motion.button
                      type="button"
                      onClick={handleSwapCurrencies}
                      whileHover={{ rotate: 180 }}
                      whileTap={{ scale: 0.9 }}
                      className="p-3 min-h-[44px] min-w-[44px] rounded-full bg-cyan-500/20 text-cyan-300 hover:bg-cyan-500/30 touch-manipulation"
                      aria-label="Valyutaları dəyiş"
                    >
                      <ChevronDown className="w-5 h-5 rotate-90" />
                    </motion.button>
                  </div>
                  <div className="p-4 rounded-xl border border-cyan-500/30 bg-gradient-to-br from-cyan-950/40 to-slate-900/80">
                    <p className="text-xs text-slate-400 mb-1">Nəticə</p>
                    <div className="flex items-center justify-between gap-2">
                      <p className="text-2xl font-bold text-white tabular-nums">{result}</p>
                      <span className="text-lg font-semibold text-cyan-400">{toCurrency}</span>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      type="button"
                      onClick={handleCopyResult}
                      variant="outline"
                      size="sm"
                      className="flex-1 border-slate-600 min-h-11"
                    >
                      <Copy className="w-4 h-4 mr-2" />
                      Kopyala
                    </Button>
                    <Button type="button" onClick={handleConvert} size="sm" className="flex-1 min-h-11">
                      Təsdiqlə
                    </Button>
                  </div>
                </div>
              )}
            </div>
          </motion.div>
        ) : null}
      </AnimatePresence>

      <motion.button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        whileHover={{ scale: 1.06 }}
        whileTap={{ scale: 0.94 }}
        className="w-14 h-14 min-h-[56px] min-w-[56px] rounded-full bg-gradient-to-br from-cyan-600 to-blue-700 text-white shadow-xl shadow-cyan-900/40 flex items-center justify-center touch-manipulation ring-2 ring-white/10"
        aria-label="Valyuta kalkulyatoru"
        aria-expanded={isOpen}
      >
        <Calculator className="h-7 w-7" />
      </motion.button>
    </div>
  );
}
