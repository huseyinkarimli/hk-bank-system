import { useMemo, useState } from 'react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { CheckCheck } from 'lucide-react';
import { toast } from 'sonner';
import { DashboardLayout } from '@/components/dashboard/layout';
import { EmptyState } from '@/components/empty-state';
import { useNotifications } from '@/context/notification-context';
import { useAuth } from '@/context/auth-context';
import { isUserAdmin } from '@/lib/user-role';

const NOTIFICATION_ICONS: Record<string, string> = {
  TRANSACTION: '💸',
  PAYMENT: '🧾',
  CARD: '💳',
  ACCOUNT: '🏦',
  SECURITY: '🔒',
  SYSTEM: '⚙️',
};

const NOTIFICATION_COLORS: Record<string, string> = {
  TRANSACTION: 'bg-blue-500/20 text-blue-300',
  PAYMENT: 'bg-green-500/20 text-green-300',
  CARD: 'bg-purple-500/20 text-purple-300',
  ACCOUNT: 'bg-cyan-500/20 text-cyan-300',
  SECURITY: 'bg-red-500/20 text-red-300',
  SYSTEM: 'bg-gray-500/20 text-gray-300',
};

function formatTimeAgo(date: Date) {
  const now = new Date();
  const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (seconds < 60) return 'indi';
  if (seconds < 3600) return `${Math.floor(seconds / 60)} dəq əvvəl`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)} saat əvvəl`;
  return `${Math.floor(seconds / 86400)} gün əvvəl`;
}

function NotificationsContent() {
  const { user } = useAuth();
  const isAdmin = isUserAdmin(user?.role);
  const {
    notifications,
    unreadCount,
    isLoading,
    markAsRead,
    markAllAsRead,
  } = useNotifications();

  const [filter, setFilter] = useState<string>('ALL');

  const getTypeCount = (type: string) => {
    if (type === 'ALL') return notifications.length;
    if (type === 'UNREAD') return notifications.filter((n) => !n.read).length;
    return notifications.filter((n) => n.type === type).length;
  };

  const filteredNotifications = useMemo(() => {
    if (filter === 'ALL') return notifications;
    if (filter === 'UNREAD') return notifications.filter((n) => !n.read);
    return notifications.filter((n) => n.type === filter);
  }, [notifications, filter]);

  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead();
      toast.success('Hamısı oxunmuş kimi işarələndi');
    } catch {
      /* interceptor */
    }
  };

  return (
    <DashboardLayout isAdmin={isAdmin}>
      <div className="max-w-4xl mx-auto">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl md:text-4xl font-bold text-white flex items-center gap-3 mb-2">
              🔔 Bildirişlər
              {unreadCount > 0 ? (
                <Badge variant="outline" className="bg-cyan-500/20 text-cyan-200 border-cyan-500/40">
                  {unreadCount}
                </Badge>
              ) : null}
            </h1>
            <p className="text-slate-400">
              {isLoading ? 'Yenilənir…' : `${filteredNotifications.length} bildiriş`}
            </p>
          </div>
          {unreadCount > 0 ? (
            <Button
              onClick={() => void handleMarkAllAsRead()}
              variant="outline"
              className="gap-2 border-slate-600 text-slate-200 hover:bg-slate-800"
            >
              <CheckCheck size={16} />
              Hamısını oxunmuş et
            </Button>
          ) : null}
        </div>

        <div className="flex flex-wrap gap-2 mb-8 pb-4 border-b border-slate-700/50 overflow-x-auto">
          {[
            { id: 'ALL', label: 'Hamısı', icon: '📋' },
            { id: 'UNREAD', label: 'Oxunmamış', icon: '📌' },
            { id: 'TRANSACTION', label: 'Köçürmə', icon: '💸' },
            { id: 'PAYMENT', label: 'Ödəniş', icon: '🧾' },
            { id: 'CARD', label: 'Kart', icon: '💳' },
            { id: 'SECURITY', label: 'Təhlükəsizlik', icon: '🔒' },
          ].map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setFilter(tab.id)}
              className={`flex items-center gap-2 px-4 py-2 rounded-full font-medium transition-all whitespace-nowrap ${
                filter === tab.id
                  ? 'bg-cyan-500 text-slate-950 shadow-lg'
                  : 'bg-slate-800/60 text-slate-400 hover:bg-slate-800'
              }`}
            >
              <span>{tab.icon}</span>
              <span>{tab.label}</span>
              {getTypeCount(tab.id) > 0 ? (
                <Badge
                  variant="secondary"
                  className={
                    filter === tab.id
                      ? 'bg-slate-900/25 text-slate-900'
                      : 'bg-slate-600 text-white'
                  }
                >
                  {getTypeCount(tab.id)}
                </Badge>
              ) : null}
            </button>
          ))}
        </div>

        {filteredNotifications.length > 0 ? (
          <div className="space-y-3">
            {filteredNotifications.map((notification) => (
              <Card
                key={notification.id}
                onClick={() => void markAsRead(notification.id)}
                className={`p-4 cursor-pointer transition-all hover:border-cyan-500/40 border-slate-700/50 bg-slate-900/40 ${
                  !notification.read
                    ? 'bg-cyan-950/20 border-l-4 border-l-cyan-500'
                    : ''
                }`}
              >
                <div className="flex items-start gap-4">
                  <div
                    className={`flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center text-lg ${NOTIFICATION_COLORS[notification.type] ?? NOTIFICATION_COLORS.SYSTEM}`}
                  >
                    {NOTIFICATION_ICONS[notification.type] ?? NOTIFICATION_ICONS.SYSTEM}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <p
                          className={`font-semibold ${!notification.read ? 'text-white' : 'text-slate-400'}`}
                        >
                          {notification.title}
                        </p>
                        <p className="text-sm text-slate-400 mt-1">{notification.message}</p>
                        <p className="text-xs text-slate-500 mt-2">
                          {formatTimeAgo(notification.timestamp)}
                        </p>
                      </div>
                      {!notification.read ? (
                        <div
                          className="flex-shrink-0 w-2 h-2 bg-cyan-400 rounded-full animate-pulse"
                          aria-hidden
                        />
                      ) : null}
                    </div>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        ) : (
          <EmptyState
            className="border-slate-700/50 bg-slate-900/40 py-14"
            icon="🔔"
            title={
              filter !== 'ALL' ? 'Bu filtr üzrə bildiriş yoxdur' : 'Hələlik bildiriş yoxdur'
            }
            description={
              filter !== 'ALL'
                ? 'Başqa filtr seçməyi sınayın.'
                : 'Əməliyyatlarınız barədə burada xəbərdar olacaqsınız.'
            }
          />
        )}
      </div>
    </DashboardLayout>
  );
}

export default function NotificationsPage() {
  return <NotificationsContent />;
}
