import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import { useAuth } from '@/context/auth-context';
import {
  closeAiSession,
  createAiSession,
  dtoToUiMessage,
  fetchSessionHistory,
  sendAiMessage,
} from '@/lib/ai-support-api';

export type ChatErrorKind = 'service_unavailable';

export interface ChatMessage {
  id?: number;
  role: 'user' | 'assistant';
  content: string;
  timestamp?: string;
  errorKind?: ChatErrorKind;
  /** User text to resend when retrying after Gemini / 503 */
  retryUserText?: string;
  /** When true, assistant bubble plays typewriter (new reply only, not history) */
  animateReply?: boolean;
}

interface AIChatContextValue {
  sessionId: string | null;
  messages: ChatMessage[];
  isSessionLoading: boolean;
  isWaitingForResponse: boolean;
  sessionError: string | null;
  sendMessage: (text: string) => Promise<void>;
  retryLastServiceError: () => Promise<void>;
  newSession: () => Promise<void>;
}

const AIChatContext = createContext<AIChatContextValue | undefined>(undefined);

const DEFAULT_GEMINI_MSG =
  'Hal-hazırda AI xidməti (Gemini) müvəqqəti əlçatan deyil. Zəhmət olmasa bir az sonra yenidən cəhd edin.';

export function AIChatProvider({ children }: { children: ReactNode }) {
  const { token, isAuthenticated, isLoading: authLoading } = useAuth();
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isSessionLoading, setIsSessionLoading] = useState(false);
  const [isWaitingForResponse, setIsWaitingForResponse] = useState(false);
  const [sessionError, setSessionError] = useState<string | null>(null);
  const initRef = useRef(false);

  const bootstrapSession = useCallback(async (t: string) => {
    setIsSessionLoading(true);
    setSessionError(null);
    try {
      const session = await createAiSession(t);
      setSessionId(session.sessionId);
      const history = await fetchSessionHistory(t, session.sessionId);
      setMessages(history.map((m) => dtoToUiMessage(m)));
    } catch (e) {
      setSessionId(null);
      setMessages([]);
      setSessionError(e instanceof Error ? e.message : 'Sessiya xətası');
    } finally {
      setIsSessionLoading(false);
    }
  }, []);

  useEffect(() => {
    if (authLoading) return;

    if (!isAuthenticated || !token) {
      initRef.current = false;
      setSessionId(null);
      setMessages([]);
      setSessionError(null);
      setIsSessionLoading(false);
      return;
    }

    if (initRef.current) return;
    initRef.current = true;
    void bootstrapSession(token);
  }, [authLoading, isAuthenticated, token, bootstrapSession]);

  const postMessageToBackend = useCallback(
    async (text: string, appendUserBubble: boolean) => {
      if (!sessionId || !token) return;

      setIsWaitingForResponse(true);
      if (appendUserBubble) {
        const userMessage: ChatMessage = {
          role: 'user',
          content: text,
          timestamp: new Date().toISOString(),
        };
        setMessages((prev) => [...prev, userMessage]);
      }

      try {
        const result = await sendAiMessage(token, sessionId, text);

        if (result.ok) {
          const ui = dtoToUiMessage(result.message);
          setMessages((prev) => [
            ...prev,
            {
              ...ui,
              timestamp: ui.timestamp ?? new Date().toISOString(),
              animateReply: true,
            },
          ]);
        } else if (result.status === 503) {
          setMessages((prev) => [
            ...prev,
            {
              role: 'assistant',
              content: result.serverMessage ?? DEFAULT_GEMINI_MSG,
              timestamp: new Date().toISOString(),
              errorKind: 'service_unavailable',
              retryUserText: text,
            },
          ]);
        } else {
          setMessages((prev) => [
            ...prev,
            {
              role: 'assistant',
              content: result.serverMessage ?? 'Mesaj göndərilərkən xəta baş verdi.',
              timestamp: new Date().toISOString(),
            },
          ]);
        }
      } catch {
        setMessages((prev) => [
          ...prev,
          {
            role: 'assistant',
            content: DEFAULT_GEMINI_MSG,
            timestamp: new Date().toISOString(),
            errorKind: 'service_unavailable',
            retryUserText: text,
          },
        ]);
      } finally {
        setIsWaitingForResponse(false);
      }
    },
    [sessionId, token]
  );

  const sendMessage = useCallback(
    async (raw: string) => {
      const text = raw.trim();
      if (!text) return;
      await postMessageToBackend(text, true);
    },
    [postMessageToBackend]
  );

  const retryLastServiceError = useCallback(async () => {
    const last = [...messages].reverse().find((m) => m.errorKind === 'service_unavailable');
    const retryText = last?.retryUserText;
    if (!retryText || !sessionId || !token) return;

    setMessages((prev) =>
      prev.filter((m) => !(m.errorKind === 'service_unavailable' && m.retryUserText === retryText))
    );
    await postMessageToBackend(retryText, false);
  }, [messages, sessionId, token, postMessageToBackend]);

  const newSession = useCallback(async () => {
    if (!token) return;
    const prevId = sessionId;
    setMessages([]);
    setSessionError(null);
    setIsSessionLoading(true);
    try {
      if (prevId) {
        try {
          await closeAiSession(token, prevId);
        } catch {
          /* best-effort */
        }
      }
      const session = await createAiSession(token);
      setSessionId(session.sessionId);
      const history = await fetchSessionHistory(token, session.sessionId);
      setMessages(history.map((m) => dtoToUiMessage(m)));
    } catch (e) {
      setSessionError(e instanceof Error ? e.message : 'Yeni sessiya açılmadı');
    } finally {
      setIsSessionLoading(false);
    }
  }, [token, sessionId]);

  const value: AIChatContextValue = {
    sessionId,
    messages,
    isSessionLoading,
    isWaitingForResponse,
    sessionError,
    sendMessage,
    retryLastServiceError,
    newSession,
  };

  return <AIChatContext.Provider value={value}>{children}</AIChatContext.Provider>;
}

export function useAIChat(): AIChatContextValue {
  const ctx = useContext(AIChatContext);
  if (!ctx) {
    throw new Error('useAIChat must be used within AIChatProvider');
  }
  return ctx;
}
