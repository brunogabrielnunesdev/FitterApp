import { api } from '../../../common/services/api'
import type { AdminModality } from '../types/modality'

const basePath = '/api/v1/admin/modalities'

export async function listAdminModalities() {
  const response = await api.get<AdminModality[]>(basePath)
  return response.data
}

export async function createAdminModality(name: string) {
  const response = await api.post<AdminModality>(basePath, { name })
  return response.data
}

export async function updateAdminModality(id: number, name: string) {
  const response = await api.put<AdminModality>(`${basePath}/${id}`, { name })
  return response.data
}

export async function setAdminModalityActive(id: number, active: boolean) {
  const response = await api.patch<AdminModality>(`${basePath}/${id}/activation`, { active })
  return response.data
}
