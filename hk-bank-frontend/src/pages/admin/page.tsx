import { useAuth } from '@/context/auth-context';
import { ProtectedRoute } from '@/components/protected-route';
import { DashboardLayout } from '@/components/dashboard/layout';
import { Navigate } from 'react-router-dom';

function AdminContent() {
  const { user } = useAuth();

  if (user?.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <DashboardLayout isAdmin>
      <div className="max-w-2xl">
        <h1 className="text-2xl font-bold text-white mb-2">Admin Panel</h1>
        <p className="text-slate-400">
          Use the dashboard overview for system-wide metrics. Full admin tools can be added here as
          routes grow.
        </p>
      </div>
    </DashboardLayout>
  );
}

export default function AdminPage() {
  return (
    <ProtectedRoute>
      <AdminContent />
    </ProtectedRoute>
  );
}
