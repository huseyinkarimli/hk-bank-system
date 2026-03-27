import { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  size?: 'sm' | 'md' | 'lg';
}

const sizeMap = {
  sm: 'max-w-md',
  md: 'max-w-lg',
  lg: 'max-w-2xl',
};

export function Modal({ isOpen, onClose, title, children, size = 'md' }: ModalProps) {
  return (
    <AnimatePresence>
      {isOpen ? (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm"
          />
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            className="fixed inset-0 z-50 flex items-center justify-center p-4"
          >
            <div
              className={`${sizeMap[size]} w-full rounded-xl border border-white/10 bg-gradient-to-br from-slate-900/95 to-slate-950/95 shadow-2xl backdrop-blur-xl`}
            >
              <div className="flex items-center justify-between border-b border-white/10 p-6">
                <h2 className="text-xl font-semibold text-white">{title}</h2>
                <button
                  type="button"
                  onClick={onClose}
                  className="rounded-lg p-1 transition hover:bg-white/10"
                >
                  <X className="h-5 w-5 text-slate-400" />
                </button>
              </div>
              <div className="max-h-96 overflow-y-auto p-6">{children}</div>
            </div>
          </motion.div>
        </>
      ) : null}
    </AnimatePresence>
  );
}

interface DepositWithdrawModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (amount: number, type: 'deposit' | 'withdraw') => Promise<void>;
  isLoading?: boolean;
  accountLabel?: string;
}

export function DepositWithdrawModal({
  isOpen,
  onClose,
  onSubmit,
  isLoading = false,
  accountLabel,
}: DepositWithdrawModalProps) {
  const [amount, setAmount] = useState('');
  const [type, setType] = useState<'deposit' | 'withdraw'>('deposit');

  useEffect(() => {
    if (!isOpen) {
      setAmount('');
      setType('deposit');
    }
  }, [isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!amount) return;
    const n = parseFloat(amount);
    if (!Number.isFinite(n) || n <= 0) return;
    await onSubmit(n, type);
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={type === 'deposit' ? 'Deposit Funds' : 'Withdraw Funds'} size="md">
      <form onSubmit={handleSubmit} className="space-y-4">
        {accountLabel ? (
          <div className="rounded-lg border border-white/10 bg-white/5 p-3 backdrop-blur-sm">
            <p className="text-sm text-slate-400">Account</p>
            <p className="font-medium text-white">{accountLabel}</p>
          </div>
        ) : null}

        <div>
          <p className="mb-2 block text-sm font-medium text-slate-300">Operation Type</p>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setType('deposit')}
              className={`flex-1 rounded-lg px-4 py-2 font-medium transition ${
                type === 'deposit'
                  ? 'bg-emerald-500 text-white shadow-lg shadow-emerald-500/25'
                  : 'bg-slate-800 text-slate-400 hover:bg-slate-700'
              }`}
            >
              Deposit
            </button>
            <button
              type="button"
              onClick={() => setType('withdraw')}
              className={`flex-1 rounded-lg px-4 py-2 font-medium transition ${
                type === 'withdraw'
                  ? 'bg-red-500 text-white shadow-lg shadow-red-500/25'
                  : 'bg-slate-800 text-slate-400 hover:bg-slate-700'
              }`}
            >
              Withdraw
            </button>
          </div>
        </div>

        <div>
          <label htmlFor="admin-fund-amount" className="mb-2 block text-sm font-medium text-slate-300">
            Amount
          </label>
          <input
            id="admin-fund-amount"
            type="number"
            step="0.01"
            min="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="Enter amount"
            className="w-full rounded-lg border border-white/10 bg-slate-900/80 px-4 py-2 text-white placeholder:text-slate-500 focus:border-cyan-500/50 focus:outline-none focus:ring-1 focus:ring-cyan-500/30"
            required
          />
        </div>

        <div className="flex gap-2 pt-4">
          <Button type="button" variant="outline" className="flex-1 border-white/15 bg-transparent" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" className="flex-1 bg-cyan-600 hover:bg-cyan-500" disabled={isLoading}>
            {isLoading ? 'Processing...' : 'Submit'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}

interface ActionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: Record<string, string>) => Promise<void>;
  isLoading?: boolean;
  title: string;
  fields: Array<{
    name: string;
    label: string;
    type: 'text' | 'select' | 'textarea';
    options?: Array<{ value: string; label: string }>;
    placeholder?: string;
  }>;
}

export function ActionModal({ isOpen, onClose, onSubmit, isLoading = false, title, fields }: ActionModalProps) {
  const [formData, setFormData] = useState<Record<string, string>>(() =>
    fields.reduce<Record<string, string>>((acc, field) => ({ ...acc, [field.name]: '' }), {})
  );

  useEffect(() => {
    if (!isOpen) return;
    setFormData(fields.reduce<Record<string, string>>((acc, field) => ({ ...acc, [field.name]: '' }), {}));
    // eslint-disable-next-line react-hooks/exhaustive-deps -- reset only when modal opens; field defs are static per usage
  }, [isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit(formData);
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title} size="md">
      <form onSubmit={handleSubmit} className="space-y-4">
        {fields.map((field) => (
          <div key={field.name}>
            <label htmlFor={`field-${field.name}`} className="mb-2 block text-sm font-medium text-slate-300">
              {field.label}
            </label>
            {field.type === 'select' ? (
              <select
                id={`field-${field.name}`}
                value={formData[field.name]}
                onChange={(e) => setFormData((prev) => ({ ...prev, [field.name]: e.target.value }))}
                className="w-full rounded-lg border border-white/10 bg-slate-900/80 px-4 py-2 text-white focus:border-cyan-500/50 focus:outline-none"
                required
              >
                <option value="">Select {field.label.toLowerCase()}</option>
                {field.options?.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            ) : field.type === 'textarea' ? (
              <textarea
                id={`field-${field.name}`}
                value={formData[field.name]}
                onChange={(e) => setFormData((prev) => ({ ...prev, [field.name]: e.target.value }))}
                placeholder={field.placeholder}
                rows={3}
                className="w-full rounded-lg border border-white/10 bg-slate-900/80 px-4 py-2 text-white placeholder:text-slate-500 focus:border-cyan-500/50 focus:outline-none"
                required
              />
            ) : (
              <input
                id={`field-${field.name}`}
                type={field.type}
                value={formData[field.name]}
                onChange={(e) => setFormData((prev) => ({ ...prev, [field.name]: e.target.value }))}
                placeholder={field.placeholder}
                className="w-full rounded-lg border border-white/10 bg-slate-900/80 px-4 py-2 text-white placeholder:text-slate-500 focus:border-cyan-500/50 focus:outline-none"
                required
              />
            )}
          </div>
        ))}

        <div className="flex gap-2 pt-4">
          <Button type="button" variant="outline" className="flex-1 border-white/15 bg-transparent" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" className="flex-1 bg-cyan-600 hover:bg-cyan-500" disabled={isLoading}>
            {isLoading ? 'Processing...' : 'Submit'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
