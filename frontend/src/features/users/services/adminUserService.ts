import { api } from '../../../common/services/api'
import type {
  AdminUserDetail,
  AdminUserPage,
  UserRoleFilter,
  UserStatusFilter,
} from '../types/adminUser'

const basePath = '/api/v1/admin/users'

export type ListAdminUsersParams = {
  query: string
  status: UserStatusFilter
  role: UserRoleFilter
  page: number
  size?: number
}

export async function listAdminUsers({
  query,
  status,
  role,
  page,
  size = 20,
}: ListAdminUsersParams) {
  const response = await api.get<AdminUserPage>(basePath, {
    params: {
      page,
      size,
      ...(query ? { query } : {}),
      ...(status === 'ALL' ? {} : { status }),
      ...(role === 'ALL' ? {} : { role }),
    },
  })
  return response.data
}

export async function getAdminUser(userId: string) {
  const response = await api.get<AdminUserDetail>(`${basePath}/${userId}`)
  return response.data
}
