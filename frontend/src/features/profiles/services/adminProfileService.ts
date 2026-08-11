import { api } from '../../../common/services/api'
import type { AdminProfileDetail } from '../types/profileDetail'

const basePath = '/api/v1/admin/personal-profiles'

export async function getAdminProfile(profileId: string) {
  const response = await api.get<AdminProfileDetail>(`${basePath}/${profileId}`)
  return response.data
}
