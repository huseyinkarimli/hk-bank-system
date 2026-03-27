export function unwrapApiData<T>(payload: unknown): T {
  if (payload !== null && typeof payload === 'object' && 'data' in payload) {
    return (payload as { data: T }).data;
  }
  return payload as T;
}

export async function apiFetch<T>(path: string, token: string | null, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const res = await fetch(path, { ...init, headers });
  if (!res.ok) {
    const err = new Error(`${res.status} ${res.statusText}`);
    throw err;
  }
  const json = await res.json();
  return unwrapApiData<T>(json);
}
