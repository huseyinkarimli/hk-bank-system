import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Menu,
  X,
  LogOut,
  LayoutDashboard,
  Users,
  CreditCard,
  ArrowRightLeft,
  FileText,
  Wallet,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { useAuth } from '@/context/auth-context';

export type AdminTabId = 'overview' | 'users' | 'accounts' | 'cards' | 'transactions' | 'audit';

const navItems: { id: AdminTabId; label: string; icon: typeof LayoutDashboard }[] = [
  { id: 'overview', label: 'Dashboard', icon: LayoutDashboard },
  { id: 'users', label: 'Users', icon: Users },
  { id: 'accounts', label: 'Accounts', icon: Wallet },
  { id: 'cards', label: 'Cards', icon: CreditCard },
  { id: 'transactions', label: 'Transactions', icon: ArrowRightLeft },
  { id: 'audit', label: 'Audit Logs', icon: FileText },
];

interface AdminLayoutProps {
  children: React.ReactNode;
  title: string;
  activeTab: AdminTabId;
  onTabChange: (tab: AdminTabId) => void;
}

export function AdminLayout({ children, title, activeTab, onTabChange }: AdminLayoutProps) {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/auth/login', { replace: true });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950">
      <header className="sticky top-0 z-40 border-b border-white/10 bg-slate-950/70 backdrop-blur-xl">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
          <button
            type="button"
            onClick={() => onTabChange('overview')}
            className="flex items-center gap-3 transition hover:opacity-90"
          >
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-cyan-500 to-blue-600 shadow-lg shadow-cyan-500/20">
              <span className="text-sm font-bold text-white">HK</span>
            </div>
            <h1 className="hidden text-xl font-bold text-white sm:block">{title}</h1>
          </button>
          <button
            type="button"
            onClick={() => setMenuOpen(!menuOpen)}
            className="rounded-lg p-2 transition hover:bg-white/10 md:hidden"
          >
            {menuOpen ? <X className="h-6 w-6 text-slate-300" /> : <Menu className="h-6 w-6 text-slate-300" />}
          </button>
          <div className="hidden items-center gap-3 md:flex">
            <Link
              to="/dashboard"
              className="text-sm text-slate-400 transition hover:text-white"
            >
              ← Main app
            </Link>
            <Button onClick={handleLogout} variant="outline" size="sm" className="gap-2 border-white/15 bg-transparent">
              <LogOut className="h-4 w-4" />
              Logout
            </Button>
          </div>
        </div>
      </header>

      <div className="flex">
        {menuOpen ? (
          <button
            type="button"
            className="fixed inset-0 z-20 bg-black/50 md:hidden"
            aria-label="Close menu"
            onClick={() => setMenuOpen(false)}
          />
        ) : null}

        <nav
          className={cn(
            'fixed z-30 h-[calc(100vh-4rem)] w-56 overflow-y-auto border-r border-white/10 bg-slate-950/90 p-4 backdrop-blur-xl transition-transform duration-300 md:relative md:top-0 md:h-auto md:translate-x-0',
            menuOpen ? 'top-16 translate-x-0' : 'top-16 -translate-x-full md:translate-x-0'
          )}
        >
          <div className="space-y-1">
            {navItems.map(({ id, label, icon: Icon }) => (
              <button
                key={id}
                type="button"
                onClick={() => {
                  onTabChange(id);
                  setMenuOpen(false);
                }}
                className={cn(
                  'flex w-full items-center gap-3 rounded-lg px-4 py-2 text-left font-medium transition',
                  activeTab === id
                    ? 'border border-cyan-500/30 bg-cyan-500/15 text-cyan-300'
                    : 'text-slate-300 hover:bg-white/5 hover:text-white'
                )}
              >
                <Icon className="h-5 w-5 shrink-0" />
                {label}
              </button>
            ))}
          </div>

          <div className="mt-8 border-t border-white/10 pt-6 md:hidden">
            <Button
              type="button"
              onClick={handleLogout}
              variant="outline"
              className="w-full justify-center gap-2 border-white/15 bg-transparent"
              size="sm"
            >
              <LogOut className="h-4 w-4" />
              Logout
            </Button>
          </div>
        </nav>

        <main className="min-h-[calc(100vh-4rem)] flex-1 overflow-x-hidden">
          <div className="mx-auto max-w-7xl p-4 md:p-6">{children}</div>
        </main>
      </div>
    </div>
  );
}
