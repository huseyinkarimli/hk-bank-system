import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { Home } from 'lucide-react';

export default function NotFoundPage() {
  return (
    <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 px-4">
      <motion.div
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.45, ease: [0.22, 1, 0.36, 1] }}
        className="max-w-md w-full text-center space-y-6"
      >
        <div className="space-y-2">
          <p className="text-8xl font-black text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-violet-500">
            404
          </p>
          <h1 className="text-2xl font-semibold text-white">Səhifə tapılmadı</h1>
          <p className="text-slate-400 text-sm leading-relaxed">
            Axtardığınız ünvan mövcud deyil və ya köçürülüb. Əsas səhifəyə qayıda bilərsiniz.
          </p>
        </div>
        <Button
          asChild
          size="lg"
          className="w-full gap-2 bg-gradient-to-r from-cyan-500 to-blue-600 text-white"
        >
          <Link to="/">
            <Home className="h-4 w-4" />
            Ana səhifə
          </Link>
        </Button>
        <p className="text-xs text-slate-500">Əgər bu xəta təkrarlanırsa, dəstəklə əlaqə saxlayın.</p>
      </motion.div>
    </main>
  );
}
