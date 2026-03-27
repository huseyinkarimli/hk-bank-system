import { DashboardLayout } from '@/components/dashboard/layout';
import { PaymentFlow } from '@/components/payments/PaymentFlow';
import { useAuth } from '@/context/auth-context';
import { isUserAdmin } from '@/lib/user-role';

function PaymentsContent() {
  const { user } = useAuth();
  const isAdmin = isUserAdmin(user?.role);

  return (
    <DashboardLayout isAdmin={isAdmin}>
      <div className="min-h-[calc(100vh-8rem)] p-0 md:p-0">
        <PaymentFlow />
      </div>
    </DashboardLayout>
  );
}

export default function PaymentsPage() {
  return <PaymentsContent />;
}
