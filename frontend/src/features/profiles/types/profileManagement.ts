import type { ProfileStatus, RevisionStatus } from './profileDetail'

export type AdminProfileSummary = {
  profileId: string
  revisionId: string | null
  fullName: string
  email: string
  profileStatus: ProfileStatus
  revisionStatus: RevisionStatus | null
  published: boolean
  submittedAt: string | null
  updatedAt: string
}

export type AdminProfilePage = {
  content: AdminProfileSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type ProfileModerationResponse = {
  profileId: string
  suspensionId: string
  profileStatus: ProfileStatus
  actionAt: string
}

export type ProfileStatusFilter = ProfileStatus | 'ALL'
export type ProfileLifecycleAction = 'suspend' | 'reactivate'
