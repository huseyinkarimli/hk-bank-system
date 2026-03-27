import { DashboardLayout } from '@/components/dashboard/layout';
import { ProtectedRoute } from '@/components/protected-route';
import { ChatHeader } from '@/components/ai-chat/ChatHeader';
import { ChatThread } from '@/components/ai-chat/ChatThread';
import { InputBar } from '@/components/ai-chat/InputBar';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { useAuth } from '@/context/auth-context';
import { useAIChat } from '@/context/ai-chat-context';

export default function AiSupportPage() {
  const { user } = useAuth();
  const {
    messages,
    isSessionLoading,
    isWaitingForResponse,
    sendMessage,
    newSession,
    retryLastServiceError,
    sessionError,
  } = useAIChat();

  return (
    <ProtectedRoute>
      <DashboardLayout isAdmin={user?.role === 'ADMIN'}>
        <div className="max-w-4xl mx-auto space-y-4">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-white mb-1">AI Dəstək</h1>
            <p className="text-slate-400 text-sm md:text-base">
              HK Bank köməkçisi ilə söhbət edin — kartlar, köçürmələr və təhlükəsizlik haqqında.
            </p>
          </div>

          {sessionError ? (
            <Alert
              variant="destructive"
              className="border-amber-500/40 bg-amber-950/25 text-amber-100 [&>svg]:text-amber-400"
            >
              <AlertTitle>Sessiya xətası</AlertTitle>
              <AlertDescription>{sessionError}</AlertDescription>
            </Alert>
          ) : null}

          <div
            className="flex flex-col rounded-2xl overflow-hidden border border-white/10 min-h-[calc(100vh-11rem)] md:min-h-[calc(100vh-10rem)] max-h-[calc(100dvh-6rem)] shadow-2xl shadow-black/40"
            style={{
              background:
                'linear-gradient(145deg, rgba(15,23,42,0.75) 0%, rgba(30,27,75,0.35) 50%, rgba(15,23,42,0.8) 100%)',
              backdropFilter: 'blur(20px)',
            }}
          >
            <ChatHeader
              variant="page"
              messageCount={messages.length}
              onNewSession={() => {
                void newSession();
              }}
            />
            <ChatThread
              variant="page"
              className="flex-1 min-h-0"
              messages={messages}
              isLoading={isSessionLoading}
              isWaitingForResponse={isWaitingForResponse}
              onQuickAction={(text) => void sendMessage(text)}
              onRetryServiceError={() => void retryLastServiceError()}
            />
            <InputBar
              variant="page"
              onSendMessage={(msg) => void sendMessage(msg)}
              isLoading={isWaitingForResponse}
            />
          </div>
        </div>
      </DashboardLayout>
    </ProtectedRoute>
  );
}
