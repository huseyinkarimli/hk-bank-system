/** Backend {@code ApiResponse<T>} shape for /api/ai/** */

export interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
}

export interface ChatSessionDto {
  sessionId: string;
  title?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
  messageCount?: number;
}

export interface ChatMessageDto {
  id?: number;
  role: 'USER' | 'ASSISTANT' | string;
  content: string;
  createdAt?: string;
}

const JSON_HEADERS = { 'Content-Type': 'application/json' };

function authHeaders(token: string): HeadersInit {
  return { Authorization: `Bearer ${token}` };
}

async function parseJson(res: Response): Promise<unknown> {
  const text = await res.text();
  if (!text) return {};
  try {
    return JSON.parse(text);
  } catch {
    return {};
  }
}

export function mapRole(role: string): 'user' | 'assistant' {
  return role === 'USER' ? 'user' : 'assistant';
}

export function dtoToUiMessage(d: ChatMessageDto): {
  id?: number;
  role: 'user' | 'assistant';
  content: string;
  timestamp?: string;
} {
  return {
    id: d.id,
    role: mapRole(d.role),
    content: d.content,
    timestamp: d.createdAt ?? undefined,
  };
}

export async function createAiSession(token: string): Promise<ChatSessionDto> {
  const res = await fetch('/api/ai/sessions', {
    method: 'POST',
    headers: authHeaders(token),
  });
  const body = (await parseJson(res)) as ApiEnvelope<ChatSessionDto>;
  if (!res.ok || !body?.data?.sessionId) {
    const msg = (body as ApiEnvelope<unknown>)?.message ?? res.statusText;
    throw new Error(msg || 'Session başlatıla bilmədi');
  }
  return body.data;
}

export async function fetchSessionHistory(token: string, sessionId: string): Promise<ChatMessageDto[]> {
  const res = await fetch(`/api/ai/sessions/${encodeURIComponent(sessionId)}/messages`, {
    headers: authHeaders(token),
  });
  const body = (await parseJson(res)) as ApiEnvelope<ChatMessageDto[]>;
  if (!res.ok) {
    throw new Error(body?.message ?? 'Tarixçə yüklənmədi');
  }
  return Array.isArray(body.data) ? body.data : [];
}

export type SendMessageResult =
  | { ok: true; message: ChatMessageDto }
  | { ok: false; status: number; serverMessage?: string };

export async function sendAiMessage(
  token: string,
  sessionId: string,
  text: string
): Promise<SendMessageResult> {
  const res = await fetch(`/api/ai/sessions/${encodeURIComponent(sessionId)}/messages`, {
    method: 'POST',
    headers: { ...JSON_HEADERS, ...authHeaders(token) },
    body: JSON.stringify({ message: text }),
  });
  const body = (await parseJson(res)) as ApiEnvelope<ChatMessageDto> & { message?: string };

  if (res.status === 503) {
    return {
      ok: false,
      status: 503,
      serverMessage: body?.message,
    };
  }

  if (!res.ok) {
    return {
      ok: false,
      status: res.status,
      serverMessage: body?.message ?? res.statusText,
    };
  }

  if (!body?.data?.content) {
    return {
      ok: false,
      status: res.status,
      serverMessage: body?.message ?? 'Boş cavab',
    };
  }

  return { ok: true, message: body.data };
}

export async function closeAiSession(token: string, sessionId: string): Promise<void> {
  const res = await fetch(`/api/ai/sessions/${encodeURIComponent(sessionId)}`, {
    method: 'DELETE',
    headers: authHeaders(token),
  });
  if (!res.ok) {
    const body = (await parseJson(res)) as { message?: string };
    throw new Error(body?.message ?? 'Sessiya bağlanmadı');
  }
}
