import { AuthProvider } from '@/context/auth-context'
import AuthLayout from '@/pages/auth/layout'
import DashboardPage from '@/pages/dashboard/page'
import HomePage from '@/pages/home/page'
import LoginPage from '@/pages/auth/login/page'
import RegisterPage from '@/pages/auth/register/page'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/auth" element={<AuthLayout />}>
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegisterPage />} />
          </Route>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
