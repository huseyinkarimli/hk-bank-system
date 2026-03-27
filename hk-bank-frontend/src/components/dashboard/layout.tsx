import { Sidebar } from '@/components/layout/Sidebar';
import { Header } from '@/components/dashboard/header';

interface DashboardLayoutProps {
  children: React.ReactNode;
  isAdmin?: boolean;
}

export function DashboardLayout({ children, isAdmin = false }: DashboardLayoutProps) {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950">
      <Sidebar isAdmin={isAdmin} />

      <div className="ml-0 md:ml-64 transition-all duration-300">
        <Header />
        <main className="p-4 md:p-8 pb-20">{children}</main>
      </div>
    </div>
  );
}
