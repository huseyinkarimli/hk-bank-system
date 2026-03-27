import { AnimatedBackground } from '@/components/animated-background';
import { PasswordStrengthIndicator } from '@/components/password-strength-indicator';
import { ToastContainer } from '@/components/toast';
import { useAuth, type RegisterData } from '@/context/auth-context';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { motion } from 'framer-motion';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function RegisterPage() {
  const [formData, setFormData] = useState<RegisterData>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phoneNumber: '',
  });
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [toasts, setToasts] = useState<
    Array<{ message: string; type: 'success' | 'error'; id: string; visible: boolean }>
  >([]);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const { register } = useAuth();
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

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.firstName.trim()) newErrors.firstName = 'Ad mütləqdir';
    if (!formData.lastName.trim()) newErrors.lastName = 'Soyad mütləqdir';
    if (!formData.email.trim()) newErrors.email = 'E-poçt mütləqdir';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email))
      newErrors.email = 'E-poçt formatı yanlışdır';

    if (!formData.password) newErrors.password = 'Parol mütləqdir';
    else if (formData.password.length < 8) newErrors.password = 'Parol ən azı 8 simvol olmalıdır';

    if (confirmPassword !== formData.password) newErrors.confirmPassword = 'Parollar uyğun gəlmir';

    const phoneDigits = formData.phoneNumber?.replace(/\s/g, '') ?? '';
    if (!phoneDigits) newErrors.phoneNumber = 'Telefon nömrəsi mütləqdir';
    else if (!/^\d{9}$/.test(phoneDigits))
      newErrors.phoneNumber = '9 rəqəm daxil edin (məs. 501234567)';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) {
      addToast('Formdakı xətaları düzəldin', 'error');
      return;
    }

    setIsLoading(true);

    try {
      await register(formData);
      addToast('Hesab uğurla yaradıldı', 'success');
      navigate('/dashboard');
    } catch {
      addToast('Qeydiyyat alınmadı. Yenidən cəhd edin.', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  return (
    <div className="min-h-screen flex">
      <AnimatedBackground />

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="w-full flex flex-col justify-center items-center px-6 sm:px-12 py-12"
      >
        <div className="w-full max-w-2xl">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-white mb-2">Hesab yaradın</h2>
            <p className="text-slate-400">HK Bank ilə müasir bankçılığa qoşulun</p>
          </div>

          <form
            onSubmit={handleSubmit}
            className="bg-white/5 backdrop-blur-md border border-white/10 rounded-2xl p-8 space-y-6"
          >
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName" className="text-slate-200">
                  Ad
                </Label>
                <Input
                  id="firstName"
                  name="firstName"
                  placeholder="Elvin"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                  className={`bg-white/5 border-white/10 text-white placeholder:text-slate-500 focus:border-blue-400 focus:bg-white/10 ${
                    errors.firstName ? 'border-red-500' : ''
                  }`}
                />
                {errors.firstName && <p className="text-red-400 text-sm">{errors.firstName}</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="lastName" className="text-slate-200">
                  Soyad
                </Label>
                <Input
                  id="lastName"
                  name="lastName"
                  placeholder="Namazov"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                  className={`bg-white/5 border-white/10 text-white placeholder:text-slate-500 focus:border-blue-400 focus:bg-white/10 ${
                    errors.lastName ? 'border-red-500' : ''
                  }`}
                />
                {errors.lastName && <p className="text-red-400 text-sm">{errors.lastName}</p>}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email" className="text-slate-200">
                E-poçt
              </Label>
              <Input
                id="email"
                name="email"
                type="email"
                placeholder="siz@hkbank.az"
                value={formData.email}
                onChange={handleChange}
                required
                className={`bg-white/5 border-white/10 text-white placeholder:text-slate-500 focus:border-blue-400 focus:bg-white/10 ${
                  errors.email ? 'border-red-500' : ''
                }`}
              />
              {errors.email && <p className="text-red-400 text-sm">{errors.email}</p>}
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="password" className="text-slate-200">
                  Parol
                </Label>
                <Input
                  id="password"
                  name="password"
                  type="password"
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  className={`bg-white/5 border-white/10 text-white placeholder:text-slate-500 focus:border-blue-400 focus:bg-white/10 ${
                    errors.password ? 'border-red-500' : ''
                  }`}
                />
                {errors.password && <p className="text-red-400 text-sm">{errors.password}</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword" className="text-slate-200">
                  Parolu təsdiqlə
                </Label>
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  className={`bg-white/5 border-white/10 text-white placeholder:text-slate-500 focus:border-blue-400 focus:bg-white/10 ${
                    errors.confirmPassword ? 'border-red-500' : ''
                  }`}
                />
                {errors.confirmPassword && (
                  <p className="text-red-400 text-sm">{errors.confirmPassword}</p>
                )}
              </div>
            </div>

            {formData.password && <PasswordStrengthIndicator password={formData.password} />}

            <div className="space-y-2">
              <Label htmlFor="phoneNumber" className="text-slate-200">
                Telefon nömrəsi
              </Label>
              <div className="flex">
                <span className="bg-white/5 border border-white/10 border-r-0 px-4 py-2 rounded-l-lg text-slate-400 flex items-center">
                  +994
                </span>
                <Input
                  id="phoneNumber"
                  name="phoneNumber"
                  placeholder="501234567"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  className={`bg-white/5 border-white/10 text-white placeholder:text-slate-500 focus:border-blue-400 focus:bg-white/10 rounded-r-lg rounded-l-none ${
                    errors.phoneNumber ? 'border-red-500' : ''
                  }`}
                />
              </div>
              {errors.phoneNumber && <p className="text-red-400 text-sm">{errors.phoneNumber}</p>}
            </div>

            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              type="submit"
              disabled={isLoading}
              className="w-full py-3 px-4 bg-gradient-to-r from-blue-500 to-blue-600 text-white font-semibold rounded-lg hover:from-blue-600 hover:to-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Hesab yaradılır…' : 'Hesab yarat'}
            </motion.button>
          </form>

          <p className="text-center text-slate-400 mt-6">
            Artıq hesabınız var?{' '}
            <Link
              to="/auth/login"
              className="text-blue-400 hover:text-blue-300 transition-colors font-semibold"
            >
              Daxil ol
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
