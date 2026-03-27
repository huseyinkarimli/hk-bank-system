import { CommandPalette } from '@/components/dashboard/command-palette'
import { FloatingAIChat } from '@/components/layout/FloatingAIChat'
import { AuthProvider } from '@/context/auth-context'
import { AIChatProvider } from '@/context/ai-chat-context'
import { PrivacyProvider } from '@/context/privacy-context'
import AuthLayout from '@/pages/auth/layout'
import AdminPage from '@/pages/admin/page'
import AiSupportPage from '@/pages/ai-support/page'
import CardsPage from '@/pages/cards/page'
import DashboardPage from '@/pages/dashboard/page'
import PaymentsPage from '@/pages/payments/page'
import TransfersPage from '@/pages/transfers/page'
import HomePage from '@/pages/home/page'
import LoginPage from '@/pages/auth/login/page'
import RegisterPage from '@/pages/auth/register/page'
import { Toaster } from 'sonner'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <PrivacyProvider>
          <AIChatProvider>
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/auth" element={<AuthLayout />}>
                <Route path="login" element={<LoginPage />} />
                <Route path="register" element={<RegisterPage />} />
              </Route>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/cards" element={<CardsPage />} />
              <Route path="/transfer" element={<TransfersPage />} />
              <Route path="/payments" element={<PaymentsPage />} />
              <Route path="/ai-support" element={<AiSupportPage />} />
              <Route path="/admin" element={<AdminPage />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
            <CommandPalette />
            <FloatingAIChat />
            <Toaster richColors position="top-center" theme="dark" />
          </AIChatProvider>
        </PrivacyProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
