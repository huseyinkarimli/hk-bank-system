import { useState } from 'react';
import { cn } from '@/lib/utils';
import { Sidebar } from '@/components/layout/Sidebar';
import { Header } from '@/components/dashboard/header';
import { FloatingAIChat } from '@/components/layout/FloatingAIChat';
import { CurrencyCalculatorWidget } from '@/components/layout/CurrencyCalculatorWidget';

interface LayoutProps {
  children: React.ReactNode;
  isAdmin?: boolean;
}

/** Main authenticated shell: sidebar, header, content, floating AI chat. */
export function Layout({ children, isAdmin = false }: LayoutProps) {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950">
      <Sidebar
        isAdmin={isAdmin}
        collapsed={sidebarCollapsed}
        onCollapsedChange={setSidebarCollapsed}
      />

      <div
        className={cn(
          'ml-0 transition-[margin] duration-300 ease-out',
          sidebarCollapsed ? 'md:ml-20' : 'md:ml-64'
        )}
      >
        <Header />
        <main className="p-4 md:p-8 pb-20">{children}</main>
      </div>

      <CurrencyCalculatorWidget />
      <FloatingAIChat />
    </div>
  );
}
