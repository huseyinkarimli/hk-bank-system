import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { api } from '@/lib/axios';
import { useAuth } from '@/context/auth-context';

export interface NotificationItem {
  id: string;
  type: 'TRANSACTION' | 'PAYMENT' | 'CARD' | 'ACCOUNT' | 'SECURITY' | 'SYSTEM';
  title: string;
  message: string;
  timestamp: Date;
  read: boolean;
}

interface ApiNotification {
  id: number;
  type: string;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

function mapApi(n: ApiNotification): NotificationItem {
  const type = (['TRANSACTION', 'PAYMENT', 'CARD', 'ACCOUNT', 'SECURITY', 'SYSTEM'].includes(
    n.type
  )
    ? n.type
    : 'SYSTEM') as NotificationItem['type'];
  return {
    id: String(n.id),
    type,
    title: n.title,
    message: n.message,
    read: n.isRead,
    timestamp: new Date(n.createdAt),
  };
}

interface NotificationContextType {
  notifications: NotificationItem[];
  unreadCount: number;
  bellBounce: boolean;
  isLoading: boolean;
  refresh: () => Promise<void>;
  markAsRead: (id: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  replaceLocal: (items: NotificationItem[]) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(
  undefined
);

export function NotificationProvider({ children }: { children: React.ReactNode }) {
  const { token, isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [bellBounce, setBellBounce] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const prevUnreadRef = useRef<number | null>(null);
  const isFirstPollRef = useRef(true);

  const unreadCount = useMemo(
    () => notifications.filter((n) => !n.read).length,
    [notifications]
  );

  const refresh = useCallback(async () => {
    if (!token) return;
    setIsLoading(true);
    try {
      const { data } = await api.get<{ data?: ApiNotification[] }>('/api/notifications', {
        headers: { Authorization: `Bearer ${token}` },
      });
      const list = Array.isArray(data?.data) ? data.data : [];
      const mapped = list.map(mapApi);
      setNotifications(mapped);

      const unread = mapped.filter((n) => !n.read).length;
      if (
        !isFirstPollRef.current &&
        prevUnreadRef.current !== null &&
        unread > prevUnreadRef.current
      ) {
        setBellBounce(true);
      }
      isFirstPollRef.current = false;
      prevUnreadRef.current = unread;
    } catch {
      /* keep existing list on error */
    } finally {
      setIsLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (!isAuthenticated || !token) {
      setNotifications([]);
      isFirstPollRef.current = true;
      prevUnreadRef.current = null;
      return;
    }

    void refresh();
    const id = window.setInterval(() => void refresh(), 60_000);
    return () => clearInterval(id);
  }, [isAuthenticated, token, refresh]);

  useEffect(() => {
    if (!bellBounce) return;
    const t = window.setTimeout(() => setBellBounce(false), 1200);
    return () => clearTimeout(t);
  }, [bellBounce]);

  const markAsRead = useCallback(async (id: string) => {
    if (!token) return;
    await api.put(`/api/notifications/${id}/read`, null, {
      headers: { Authorization: `Bearer ${token}` },
    });
    setNotifications((prev) => {
      const next = prev.map((n) => (n.id === id ? { ...n, read: true } : n));
      prevUnreadRef.current = next.filter((n) => !n.read).length;
      return next;
    });
  }, [token]);

  const markAllAsRead = useCallback(async () => {
    if (!token) return;
    await api.put('/api/notifications/read-all', null, {
      headers: { Authorization: `Bearer ${token}` },
    });
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    prevUnreadRef.current = 0;
  }, [token]);

  const replaceLocal = useCallback((items: NotificationItem[]) => {
    setNotifications(items);
  }, []);

  const value = useMemo(
    () => ({
      notifications,
      unreadCount,
      bellBounce,
      isLoading,
      refresh,
      markAsRead,
      markAllAsRead,
      replaceLocal,
    }),
    [
      notifications,
      unreadCount,
      bellBounce,
      isLoading,
      refresh,
      markAsRead,
      markAllAsRead,
      replaceLocal,
    ]
  );

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotifications() {
  const ctx = useContext(NotificationContext);
  if (!ctx) {
    throw new Error('useNotifications must be used within NotificationProvider');
  }
  return ctx;
}
