import { AnimatedBackground } from '@/components/animated-background';
import { ToastContainer } from '@/components/toast';
import { useAuth } from '@/context/auth-context';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { motion } from 'framer-motion';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const statCards = [
  { label: 'İstifadəçi', value: '50K+', description: 'Aktiv müştəri' },
  { label: 'Təhlükəsizlik', value: '100%', description: 'Qorunan əməliyyat' },
  { label: 'Dəstək', value: '24/7', description: 'Hər zaman əlçatan' },
];

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [toasts, setToasts] = useState<
    Array<{ message: string; type: 'success' | 'error'; id: string; visible: boolean }>
  >([]);
  const { login } = useAuth();
  const navigate = useNavigate();

  const addToast = (message: string, type: 'success' | 'error' = 'error') => {
    const id = Date.now().toString();
    const toast = { message, type, id, visible: true };
    setToasts((prev) => [...prev, toast]);

    setTimeout(() => {
      setToasts((prev) => prev.map((t) => (t.id === id ? { ...t, visible: false } : t)));
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
      }, 300);
    }, 4000);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await login(email, password);
      addToast('Uğurla daxil oldunuz', 'success');
      navigate('/dashboard');
    } catch {
      addToast('E-poçt və ya parol yanlışdır', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      <AnimatedBackground />

      <motion.div
        initial={{ opacity: 0, x: -50 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.6 }}
        className="hidden lg:flex lg:w-3/5 flex-col justify-center items-start px-12 pt-20"
      >
        <div className="space-y-8 max-w-md">
          <div>
            <h1 className="text-5xl font-bold text-white mb-2 text-balance">
              Müasir{' '}
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-cyan-400">
                bankçılıq
              </span>
            </h1>
            <p className="text-slate-300 text-lg">
              HK Bank ilə sürətli və təhlükəsiz maliyyə əməliyyatları — sizin üçün dizayn edilib.
            </p>
          </div>

          <div className="space-y-4">
            {statCards.map((stat, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1 * index, duration: 0.6 }}
                className="bg-white/5 backdrop-blur-md border border-white/10 rounded-xl p-4 hover:bg-white/10 transition-all"
              >
                <p className="text-slate-400 text-sm">{stat.label}</p>
                <p className="text-2xl font-bold text-white mt-1">{stat.value}</p>
                <p className="text-slate-500 text-xs">{stat.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, x: 50 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.6 }}
        className="w-full lg:w-2/5 flex flex-col justify-center items-center px-6 sm:px-12"
      >
        <div className="w-full max-w-md">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-white mb-2">Yenidən xoş gəldiniz</h2>
            <p className="text-slate-400">HK Bank hesabınıza daxil olun</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="email" className="text-slate-200">
                E-poçt
              </Label>
              <Input
                id="email"
                type="email"
                placeholder="siz@hkbank.az"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="bg-white/5 border-white/10 text-white placeholder:text-slate-500 focus:border-blue-400 focus:bg-white/10"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password" className="text-slate-200">
                Parol
              </Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="bg-white/5 border-white/10 text-white placeholder:text-slate-500 focus:border-blue-400 focus:bg-white/10"
              />
            </div>

            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              type="submit"
              disabled={isLoading}
              className="w-full py-3 px-4 bg-gradient-to-r from-blue-500 to-blue-600 text-white font-semibold rounded-lg hover:from-blue-600 hover:to-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Daxil olunur…' : 'Daxil ol'}
            </motion.button>
          </form>

          <p className="text-center text-slate-400 mt-6">
            Hesabınız yoxdur?{' '}
            <Link
              to="/auth/register"
              className="text-blue-400 hover:text-blue-300 transition-colors font-semibold"
            >
              Qeydiyyat
            </Link>
          </p>
        </div>
      </motion.div>

      <ToastContainer
        toasts={toasts}
        onRemove={(id) => setToasts((prev) => prev.filter((t) => t.id !== id))}
      />
    </div>
  );
}
