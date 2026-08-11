import { api } from '../../../common/services/api'
import type { AdminProfileDetail } from '../types/profileDetail'
import type {
  AdminProfilePage,
  AdminProfileSummary,
  ProfileLifecycleAction,
  ProfileModerationResponse,
  ProfileStatusFilter,
} from '../types/profileManagement'

const basePath = '/api/v1/admin/personal-profiles'

export async function getAdminProfile(profileId: string) {
  const response = await api.get<AdminProfileDetail>(`${basePath}/${profileId}`)
  return response.data
}

type ListAdminProfilesParams = {
  status: ProfileStatusFilter
  page: number
  size?: number
}

export async function listAdminProfiles({ status, page, size = 20 }: ListAdminProfilesParams) {
  const response = await api.get<AdminProfilePage>(basePath, {
    params: {
      page,
      size,
      ...(status === 'ALL' ? {} : { status }),
    },
  })
  return response.data
}

function normalizeSearch(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('pt-BR')
}

export async function searchAdminProfiles(status: ProfileStatusFilter, name: string) {
  const firstPage = await listAdminProfiles({ status, page: 0, size: 100 })
  const remainingPages = await Promise.all(
    Array.from({ length: Math.max(firstPage.totalPages - 1, 0) }, (_, index) =>
      listAdminProfiles({ status, page: index + 1, size: 100 }),
    ),
  )
  const normalizedName = normalizeSearch(name)
  return [firstPage, ...remainingPages]
    .flatMap((page) => page.content)
    .filter((profile) => normalizeSearch(profile.fullName).includes(normalizedName))
}

export function paginateProfiles(profiles: AdminProfileSummary[], page: number, size: number) {
  const start = page * size
  return profiles.slice(start, start + size)
}

export async function moderateProfile(
  profileId: string,
  action: ProfileLifecycleAction,
  reason: string,
) {
  const endpoint = action === 'suspend' ? 'suspension' : 'reactivation'
  const response = await api.patch<ProfileModerationResponse>(
    `${basePath}/${profileId}/${endpoint}`,
    { reason },
  )
  return response.data
}
