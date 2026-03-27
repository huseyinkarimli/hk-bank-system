import { useAuth } from '@/context/auth-context';
import { useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';

function AuthSpinner() {
  return (
    <div className="flex items-center justify-center min-h-screen bg-slate-950">
      <div className="w-8 h-8 border-4 border-blue-500/20 border-t-blue-500 rounded-full animate-spin" />
    </div>
  );
}

/** Legacy wrapper for single-route protection. Prefer `RequireAuth` layout routes in `App`. */
export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      navigate('/auth/login', { replace: true });
    }
  }, [isAuthenticated, isLoading, navigate]);

  if (isLoading) {
    return <AuthSpinner />;
  }

  if (!isAuthenticated) {
    return null;
  }

  return <>{children}</>;
}

/** Renders nested routes only when the user is authenticated. */
export function RequireAuth() {
  const { isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      navigate('/auth/login', { replace: true });
    }
  }, [isAuthenticated, isLoading, navigate]);

  if (isLoading) {
    return <AuthSpinner />;
  }

  if (!isAuthenticated) {
    return null;
  }

  return <Outlet />;
}

/** Nested layout: authenticated user must have ADMIN role. */
export function RequireAdmin() {
  const { user, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading && user && user.role !== 'ADMIN') {
      navigate('/dashboard', { replace: true });
    }
  }, [isLoading, user, navigate]);

  if (isLoading) {
    return <AuthSpinner />;
  }

  if (user?.role !== 'ADMIN') {
    return null;
  }

  return <Outlet />;
}
