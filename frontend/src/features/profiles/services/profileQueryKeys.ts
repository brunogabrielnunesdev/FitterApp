export const profileQueryKeys = {
  all: ['admin-personal-profiles'] as const,
  detail: (profileId: string) => ['admin-personal-profiles', 'detail', profileId] as const,
  list: (status: string, page: number, search: string) =>
    ['admin-personal-profiles', 'list', { status, page, search }] as const,
  pending: ['admin-personal-profiles', 'pending'] as const,
}
