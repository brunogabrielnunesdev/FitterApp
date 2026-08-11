import { api } from '../../../common/services/api'
import type {
  AdminModality,
  CreatePersonalRequest,
  PersonalActionResponse,
  UpdatePersonalRequest,
} from '../types/personalManagement'

const profilesPath = '/api/v1/admin/personal-profiles'

export async function listAdminModalities() {
  const response = await api.get<AdminModality[]>('/api/v1/admin/modalities')
  return response.data
}

export async function createPersonal(request: CreatePersonalRequest) {
  const response = await api.post<PersonalActionResponse>(profilesPath, request)
  return response.data
}

export async function updatePersonal(profileId: string, request: UpdatePersonalRequest) {
  const response = await api.put<PersonalActionResponse>(`${profilesPath}/${profileId}`, request)
  return response.data
}
