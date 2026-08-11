import { api } from '../../../common/services/api'
import type { PendingProfile, ProfileActionResponse } from '../types/moderation'

const basePath = '/api/v1/admin/personal-profiles'

export async function listPendingProfiles() {
  const response = await api.get<PendingProfile[]>(`${basePath}/pending-review`)
  return response.data
}

export async function approveProfile(profileId: string) {
  const response = await api.patch<ProfileActionResponse>(`${basePath}/${profileId}/approval`)
  return response.data
}

export async function rejectProfile(profileId: string, reason: string) {
  const response = await api.patch<ProfileActionResponse>(`${basePath}/${profileId}/rejection`, {
    reason,
  })
  return response.data
}
