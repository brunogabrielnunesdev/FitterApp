import type { UserRole, UserStatus } from '../types/adminUser'

export const userStatusLabels: Record<UserStatus, string> = {
  PENDING_VERIFICATION: 'Verificação pendente',
  ACTIVE: 'Ativa',
  BLOCKED: 'Bloqueada',
}

export const userRoleLabels: Record<UserRole, string> = {
  STUDENT: 'Aluno',
  PERSONAL: 'Personal',
  ADMIN: 'Administrador',
  OWNER: 'Proprietário',
}

export const userStatusStyles: Record<UserStatus, string> = {
  PENDING_VERIFICATION: 'border-[#ff9f43]/40 bg-[#ff9f43]/10 text-[#ffc27e]',
  ACTIVE: 'border-[#c7ff3d]/40 bg-[#c7ff3d]/10 text-[#dfff8d]',
  BLOCKED: 'border-[#ff6b6b]/40 bg-[#ff6b6b]/10 text-[#ff9b9b]',
}
