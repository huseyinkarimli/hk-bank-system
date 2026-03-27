import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  Wallet,
  TrendingUp,
  CreditCard,
  Settings,
  LogOut,
  Menu,
  X,
  Home,
  Bell,
  MoreHorizontal,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { useAuth } from '@/context/auth-context';

interface SidebarProps {
  isAdmin?: boolean;
}

export function Sidebar({ isAdmin = false }: SidebarProps) {
  const [isOpen, setIsOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { logout } = useAuth();

  const menuItems = [
    { icon: Home, label: 'Dashboard', to: '/dashboard' },
    { icon: Wallet, label: 'Accounts', to: '/dashboard' },
    { icon: CreditCard, label: 'Cards', to: '/dashboard' },
    { icon: TrendingUp, label: 'Investments', to: '/dashboard' },
    { icon: Bell, label: 'Notifications', to: '/dashboard', badge: 3 },
  ];

  const adminItems = isAdmin
    ? [{ icon: Settings, label: 'Admin Panel', to: '/admin' }]
    : [];

  const handleLogout = () => {
    logout();
    navigate('/auth/login', { replace: true });
  };

  return (
    <>
      <Button
        variant="ghost"
        size="icon"
        onClick={() => setIsOpen(!isOpen)}
        className="md:hidden fixed top-4 left-4 z-50"
      >
        {isOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
      </Button>

      {isOpen && (
        <div
          className="fixed inset-0 bg-black/50 md:hidden z-30"
          onClick={() => setIsOpen(false)}
          aria-hidden
        />
      )}

      <aside
        className={cn(
          'fixed left-0 top-0 h-screen w-64 bg-gradient-to-b from-slate-900 via-slate-900 to-slate-950 border-r border-slate-700/50 backdrop-blur-xl transition-all duration-300 z-40',
          !isOpen && '-translate-x-full md:translate-x-0'
        )}
      >
        <div className="flex flex-col h-full p-6">
          <div className="flex items-center gap-3 mb-12">
            <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500 to-cyan-400 flex items-center justify-center">
              <Wallet className="h-6 w-6 text-white" />
            </div>
            <h1 className="text-xl font-bold text-white">HKBank</h1>
          </div>

          <nav className="flex-1 space-y-2">
            {menuItems.map((item) => {
              const active =
                item.label === 'Dashboard'
                  ? location.pathname === '/dashboard'
                  : location.pathname === item.to && item.to !== '/dashboard';
              return (
                <Link
                  key={item.label}
                  to={item.to}
                  onClick={() => setIsOpen(false)}
                >
                  <div
                    className={cn(
                      'flex items-center justify-between px-4 py-3 rounded-lg transition-all duration-200',
                      active
                        ? 'bg-gradient-to-r from-blue-500 to-cyan-400 text-white shadow-lg shadow-blue-500/30'
                        : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
                    )}
                  >
                    <div className="flex items-center gap-3">
                      <item.icon className="h-5 w-5" />
                      <span className="font-medium">{item.label}</span>
                    </div>
                    {item.badge ? (
                      <span className="text-xs font-semibold bg-red-500 text-white px-2 py-1 rounded-full">
                        {item.badge}
                      </span>
                    ) : null}
                  </div>
                </Link>
              );
            })}

            {adminItems.length > 0 && (
              <>
                <div className="my-4 px-4 border-t border-slate-700/50" />
                <div className="text-xs font-semibold text-slate-500 uppercase px-4 mb-2">
                  Admin
                </div>
                {adminItems.map((item) => (
                  <Link
                    key={item.label}
                    to={item.to}
                    onClick={() => setIsOpen(false)}
                  >
                    <div
                      className={cn(
                        'flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200',
                        location.pathname === item.to
                          ? 'bg-slate-800 text-cyan-400'
                          : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
                      )}
                    >
                      <item.icon className="h-5 w-5" />
                      <span className="font-medium">{item.label}</span>
                    </div>
                  </Link>
                ))}
              </>
            )}
          </nav>

          <div className="space-y-2 border-t border-slate-700/50 pt-4">
            <button
              type="button"
              className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-slate-400 hover:bg-slate-800/50 hover:text-slate-200 transition-all duration-200"
            >
              <MoreHorizontal className="h-5 w-5" />
              <span className="font-medium">More</span>
            </button>
            <button
              type="button"
              onClick={handleLogout}
              className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-slate-400 hover:bg-red-500/10 hover:text-red-400 transition-all duration-200"
            >
              <LogOut className="h-5 w-5" />
              <span className="font-medium">Logout</span>
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}
