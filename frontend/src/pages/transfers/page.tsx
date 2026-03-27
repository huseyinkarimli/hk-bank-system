import { DashboardLayout } from '@/components/dashboard/layout';
import { TransferFlow } from '@/components/transfers/TransferFlow';
import { useAuth } from '@/context/auth-context';
import { isUserAdmin } from '@/lib/user-role';

function TransfersContent() {
  const { user } = useAuth();
  const isAdmin = isUserAdmin(user?.role);

  return (
    <DashboardLayout isAdmin={isAdmin}>
      <div className="min-h-[calc(100vh-8rem)] p-0 md:p-0">
        <TransferFlow />
      </div>
    </DashboardLayout>
  );
}

export default function TransfersPage() {
  return <TransfersContent />;
}
