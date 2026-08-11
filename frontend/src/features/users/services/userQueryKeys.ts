import type { UserRoleFilter, UserStatusFilter } from '../types/adminUser'

export const userQueryKeys = {
  all: ['admin-users'] as const,
  list: (
    query: string,
    status: UserStatusFilter,
    role: UserRoleFilter,
    page: number,
  ) => ['admin-users', 'list', { query, status, role, page }] as const,
  detail: (userId: string) => ['admin-users', 'detail', userId] as const,
}
