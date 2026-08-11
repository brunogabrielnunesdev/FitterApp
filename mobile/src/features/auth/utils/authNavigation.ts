const authRoutes = new Set([
  '/login',
  '/register',
  '/forgot-password',
  '/confirm-email',
  '/reset-password',
]);

export function getSafeReturnPath(value: string | string[] | undefined, fallback = '/home') {
  const path = Array.isArray(value) ? value[0] : value;
  if (!path || !path.startsWith('/') || path.startsWith('//')) return fallback;
  if (authRoutes.has(path.split('?')[0])) return fallback;
  return path;
}

export function withReturnPath(path: string, returnPath: string) {
  return `${path}?returnTo=${encodeURIComponent(returnPath)}`;
}
