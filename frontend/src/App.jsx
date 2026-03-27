import { lazy, Suspense } from 'react'
import { CommandPalette } from '@/components/dashboard/command-palette'
import { AnimatedLayout } from '@/components/animated-layout'
import { AuthProvider } from '@/context/auth-context'
import { AIChatProvider } from '@/context/ai-chat-context'
import { PrivacyProvider } from '@/context/privacy-context'
import { ThemeProvider } from '@/context/theme-context'
import { NotificationProvider } from '@/context/notification-context'
import { RequireAdmin, RequireAuth } from '@/components/protected-route'
import { ErrorBoundary } from '@/components/error-boundary'
import { SoundClickRoot } from '@/components/sound-click-root'
import { AppToaster } from '@/components/AppToaster'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'

const HomePage = lazy(() => import('@/pages/home/page'))
const AuthLayout = lazy(() => import('@/pages/auth/layout'))
const LoginPage = lazy(() => import('@/pages/auth/login/page'))
const RegisterPage = lazy(() => import('@/pages/auth/register/page'))
const DashboardPage = lazy(() => import('@/pages/dashboard/page'))
const AccountsPage = lazy(() => import('@/pages/accounts/page'))
const ProfilePage = lazy(() => import('@/pages/profile/page'))
const NotificationsPage = lazy(() => import('@/pages/notifications/page'))
const CardsPage = lazy(() => import('@/pages/cards/page'))
const TransfersPage = lazy(() => import('@/pages/transfers/page'))
const PaymentsPage = lazy(() => import('@/pages/payments/page'))
const AiSupportPage = lazy(() => import('@/pages/ai-support/page'))
const AdminPage = lazy(() => import('@/pages/admin/page'))
const NotFoundPage = lazy(() => import('@/pages/not-found/page'))

function RouteFallback() {
  return (
    <div className="flex min-h-[45vh] items-center justify-center bg-slate-950">
      <div
        className="h-10 w-10 border-2 border-cyan-500/25 border-t-cyan-500 rounded-full animate-spin"
        aria-hidden
      />
    </div>
  )
}

function SuspensePage({ children }) {
  return (
    <ErrorBoundary>
      <Suspense fallback={<RouteFallback />}>{children}</Suspense>
    </ErrorBoundary>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <ThemeProvider>
        <SoundClickRoot />
        <AuthProvider>
          <PrivacyProvider>
            <AIChatProvider>
              <NotificationProvider>
                <Routes>
                  <Route element={<AnimatedLayout />}>
                    <Route
                      path="/"
                      element={
                        <SuspensePage>
                          <HomePage />
                        </SuspensePage>
                      }
                    />
                    <Route
                      path="/auth"
                      element={
                        <SuspensePage>
                          <AuthLayout />
                        </SuspensePage>
                      }
                    >
                      <Route index element={<Navigate to="login" replace />} />
                      <Route
                        path="login"
                        element={
                          <SuspensePage>
                            <LoginPage />
                          </SuspensePage>
                        }
                      />
                      <Route
                        path="register"
                        element={
                          <SuspensePage>
                            <RegisterPage />
                          </SuspensePage>
                        }
                      />
                    </Route>

                    <Route element={<RequireAuth />}>
                      <Route
                        path="/dashboard"
                        element={
                          <SuspensePage>
                            <DashboardPage />
                          </SuspensePage>
                        }
                      />
                      <Route
                        path="/accounts"
                        element={
                          <SuspensePage>
                            <AccountsPage />
                          </SuspensePage>
                        }
                      />
                      <Route
                        path="/profile"
                        element={
                          <SuspensePage>
                            <ProfilePage />
                          </SuspensePage>
                        }
                      />
                      <Route
                        path="/notifications"
                        element={
                          <SuspensePage>
                            <NotificationsPage />
                          </SuspensePage>
                        }
                      />
                      <Route
                        path="/cards"
                        element={
                          <SuspensePage>
                            <CardsPage />
                          </SuspensePage>
                        }
                      />
                      <Route
                        path="/transfer"
                        element={
                          <SuspensePage>
                            <TransfersPage />
                          </SuspensePage>
                        }
                      />
                      <Route
                        path="/payments"
                        element={
                          <SuspensePage>
                            <PaymentsPage />
                          </SuspensePage>
                        }
                      />
                      <Route
                        path="/ai-support"
                        element={
                          <SuspensePage>
                            <AiSupportPage />
                          </SuspensePage>
                        }
                      />
                      <Route element={<RequireAdmin />}>
                        <Route
                          path="/admin"
                          element={
                            <SuspensePage>
                              <AdminPage />
                            </SuspensePage>
                          }
                        />
                      </Route>
                    </Route>

                    <Route
                      path="*"
                      element={
                        <SuspensePage>
                          <NotFoundPage />
                        </SuspensePage>
                      }
                    />
                  </Route>
                </Routes>
                <CommandPalette />
                <AppToaster />
              </NotificationProvider>
            </AIChatProvider>
          </PrivacyProvider>
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  )
}
