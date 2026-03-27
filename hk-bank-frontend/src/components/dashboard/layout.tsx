import { Layout } from '@/components/Layout';

interface DashboardLayoutProps {
  children: React.ReactNode;
  isAdmin?: boolean;
}

export function DashboardLayout({ children, isAdmin = false }: DashboardLayoutProps) {
  return <Layout isAdmin={isAdmin}>{children}</Layout>;
}
