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
  ArrowLeftRight,
  Banknote,
  MessageCircle,
  UserRound,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { useAuth } from '@/context/auth-context';
import { useNotifications } from '@/context/notification-context';

interface SidebarProps {
  isAdmin?: boolean;
  collapsed?: boolean;
  onCollapsedChange?: (collapsed: boolean) => void;
}

export function Sidebar({
  isAdmin = false,
  collapsed = false,
  onCollapsedChange,
}: SidebarProps) {
  const [isOpen, setIsOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const { unreadCount, bellBounce } = useNotifications();
  const showAdminLink = user?.role === 'ADMIN' || isAdmin;

  const menuItems = [
    { icon: Home, label: 'Dashboard', to: '/dashboard', match: (p: string) => p === '/dashboard' },
    { icon: Wallet, label: 'Hesablar', to: '/accounts', match: (p: string) => p === '/accounts' },
    { icon: CreditCard, label: 'Kartlar', to: '/cards', match: (p: string) => p === '/cards' },
    {
      icon: ArrowLeftRight,
      label: 'Köçürmə',
      to: '/transfer',
      match: (p: string) => p === '/transfer',
    },
    { icon: Banknote, label: 'Ödənişlər', to: '/payments', match: (p: string) => p === '/payments' },
    {
      icon: MessageCircle,
      label: 'AI Dəstək',
      to: '/ai-support',
      match: (p: string) => p === '/ai-support',
    },
    {
      icon: TrendingUp,
      label: 'İnvestisiya',
      to: '/dashboard',
      match: (p: string) => false,
    },
    {
      icon: Bell,
      label: 'Bildirişlər',
      to: '/notifications',
      match: (p: string) => p === '/notifications',
      badge: unreadCount > 0 ? unreadCount : undefined,
      bellMotion: true,
    },
    {
      icon: UserRound,
      label: 'Profil',
      to: '/profile',
      match: (p: string) => p === '/profile',
    },
  ];

  const adminItems = showAdminLink ? [{ icon: Settings, label: 'İnzibatçı paneli', to: '/admin' }] : [];

  const handleLogout = () => {
    logout();
    navigate('/auth/login', { replace: true });
  };

  const asideWidth = collapsed ? 'md:w-20' : 'md:w-64';

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

      {isOpen ? (
        <div
          className="fixed inset-0 bg-black/50 md:hidden z-30"
          onClick={() => setIsOpen(false)}
          aria-hidden
        />
      ) : null}

      <aside
        className={cn(
          'fixed left-0 top-0 h-screen bg-gradient-to-b from-slate-900 via-slate-900 to-slate-950 border-r border-slate-700/50 backdrop-blur-xl transition-all duration-300 z-40 w-64',
          asideWidth,
          !isOpen && '-translate-x-full md:translate-x-0'
        )}
      >
        <div className="flex flex-col h-full p-4 md:p-6">
          <div
            className={cn(
              'flex items-center mb-8 gap-2',
              collapsed ? 'md:flex-col md:items-stretch' : 'justify-between'
            )}
          >
            <div className="flex items-center gap-3 min-w-0 flex-1">
              <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500 to-cyan-400 flex items-center justify-center shrink-0">
                <Wallet className="h-6 w-6 text-white" />
              </div>
              {!collapsed ? (
                <h1 className="text-xl font-bold text-white truncate">HKBank</h1>
              ) : null}
            </div>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="hidden md:flex text-slate-400 hover:text-white shrink-0"
              onClick={() => onCollapsedChange?.(!collapsed)}
              aria-label={collapsed ? 'Menyunu genişləndir' : 'Menyunu yığışdır'}
            >
              {collapsed ? <ChevronRight className="h-5 w-5" /> : <ChevronLeft className="h-5 w-5" />}
            </Button>
          </div>

          <nav className="flex-1 space-y-2 overflow-y-auto">
            {menuItems.map((item) => {
              const active = item.match(location.pathname);
              const showLabel = !collapsed;
              return (
                <Link
                  key={item.label}
                  to={item.to}
                  onClick={() => setIsOpen(false)}
                  title={collapsed ? item.label : undefined}
                >
                  <div
                    className={cn(
                      'flex items-center justify-between px-3 md:px-4 py-3 rounded-lg transition-all duration-200 touch-manipulation min-h-[48px]',
                      collapsed && 'md:justify-center md:px-2',
                      active
                        ? 'bg-gradient-to-r from-blue-500 to-cyan-400 text-white shadow-lg shadow-blue-500/30'
                        : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
                    )}
                  >
                    <div className="flex items-center gap-3 min-w-0 flex-1">
                      <span className="relative inline-flex shrink-0">
                        <item.icon
                          className={cn(
                            'h-5 w-5',
                            item.bellMotion && bellBounce && 'animate-bounce'
                          )}
                        />
                        {collapsed && item.badge != null && item.badge > 0 ? (
                          <span
                            className="absolute -right-1 -top-0.5 h-2 min-w-2 rounded-full bg-red-500 ring-2 ring-slate-900"
                            aria-hidden
                          />
                        ) : null}
                      </span>
                      {showLabel ? (
                        <span className="font-medium truncate">{item.label}</span>
                      ) : null}
                    </div>
                    {item.badge != null && item.badge > 0 && showLabel ? (
                      <span className="text-xs font-semibold bg-red-500 text-white px-2 py-0.5 rounded-full shrink-0">
                        {item.badge > 99 ? '99+' : item.badge}
                      </span>
                    ) : null}
                  </div>
                </Link>
              );
            })}

            {adminItems.length > 0 ? (
              <>
                <div className="my-4 px-4 border-t border-slate-700/50" />
                <div
                  className={cn(
                    'text-xs font-semibold text-slate-500 uppercase px-4 mb-2',
                    collapsed && 'md:hidden'
                  )}
                >
                  İnzibatçı
                </div>
                {adminItems.map((item) => (
                  <Link
                    key={item.label}
                    to={item.to}
                    onClick={() => setIsOpen(false)}
                    title={collapsed ? item.label : undefined}
                  >
                    <div
                      className={cn(
                        'flex items-center gap-3 px-3 md:px-4 py-3 rounded-lg transition-all duration-200 touch-manipulation min-h-[48px]',
                        collapsed && 'md:justify-center',
                        location.pathname === item.to
                          ? 'bg-slate-800 text-cyan-400'
                          : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
                      )}
                    >
                      <item.icon className="h-5 w-5 shrink-0" />
                      {!collapsed ? <span className="font-medium">{item.label}</span> : null}
                    </div>
                  </Link>
                ))}
              </>
            ) : null}
          </nav>

          <div className="space-y-2 border-t border-slate-700/50 pt-4">
            <button
              type="button"
              className={cn(
                'w-full flex items-center gap-3 px-4 py-3 rounded-lg text-slate-400 hover:bg-slate-800/50 hover:text-slate-200 transition-all duration-200',
                collapsed && 'md:justify-center md:px-2'
              )}
            >
              <MoreHorizontal className="h-5 w-5 shrink-0" />
              {!collapsed ? <span className="font-medium">Daha çox</span> : null}
            </button>
            <button
              type="button"
              onClick={handleLogout}
              className={cn(
                'w-full flex items-center gap-3 px-4 py-3 rounded-lg text-slate-400 hover:bg-red-500/10 hover:text-red-400 transition-all duration-200',
                collapsed && 'md:justify-center md:px-2'
              )}
            >
              <LogOut className="h-5 w-5 shrink-0" />
              {!collapsed ? <span className="font-medium">Çıxış</span> : null}
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}
