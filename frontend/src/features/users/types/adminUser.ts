export type UserStatus = 'PENDING_VERIFICATION' | 'ACTIVE' | 'BLOCKED'
export type UserRole = 'STUDENT' | 'PERSONAL' | 'ADMIN' | 'OWNER'

export type AdminUserRole = {
  name: UserRole
  grantedAt: string
  grantedByUserId: string | null
}

export type AdminUserSummary = {
  userId: string
  fullName: string
  email: string
  phoneNumber: string
  status: UserStatus
  roles: AdminUserRole[]
  createdAt: string
  updatedAt: string
}

export type AdminUserDetail = AdminUserSummary & {
  emailVerifiedAt: string | null
}

export type AdminUserPage = {
  content: AdminUserSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type UserStatusFilter = UserStatus | 'ALL'
export type UserRoleFilter = UserRole | 'ALL'
