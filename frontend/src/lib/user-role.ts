/**
 * Normalizes role values from API / JWT (string enum or { name: string }).
 */
export function normalizeUserRole(role: unknown): string | undefined {
  if (role == null) return undefined;
  if (typeof role === 'string') return role;
  if (typeof role === 'object' && role !== null && 'name' in role) {
    const n = (role as { name?: unknown }).name;
    if (typeof n === 'string') return n;
  }
  return String(role);
}

export function isUserAdmin(role: unknown): boolean {
  const r = normalizeUserRole(role)?.toUpperCase() ?? '';
  return r === 'ADMIN' || r === 'ROLE_ADMIN';
}
