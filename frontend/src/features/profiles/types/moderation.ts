export type PendingProfile = {
  profileId: string
  fullName: string
  profileStatus: 'PENDING_REVIEW'
  revisionStatus: 'PENDING_REVIEW'
  rejectionReason: string | null
  published: boolean
  publishedRevisionId: string | null
}

export type ProfileActionResponse = {
  profileId: string
  revisionId: string
}

export type ModerationAction = 'approve' | 'reject'

export type ModerationFeedback = {
  profileId: string
  profileName: string
  action: ModerationAction | 'suspend' | 'reactivate'
  status: 'success' | 'error'
  message: string
}
