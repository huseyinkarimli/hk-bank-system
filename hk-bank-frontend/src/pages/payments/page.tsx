import { DashboardLayout } from '@/components/dashboard/layout';
import { ProtectedRoute } from '@/components/protected-route';
import { PaymentFlow } from '@/components/payments/PaymentFlow';
import { useAuth } from '@/context/auth-context';

function PaymentsContent() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  return (
    <DashboardLayout isAdmin={isAdmin}>
      <div className="min-h-[calc(100vh-8rem)] p-0 md:p-0">
        <PaymentFlow />
      </div>
    </DashboardLayout>
  );
}

export default function PaymentsPage() {
  return (
    <ProtectedRoute>
      <PaymentsContent />
    </ProtectedRoute>
  );
}
