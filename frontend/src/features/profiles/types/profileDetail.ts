export type ProfileStatus =
  | 'DRAFT'
  | 'PENDING_REVIEW'
  | 'APPROVED'
  | 'PUBLISHED'
  | 'REJECTED'
  | 'SUSPENDED'

export type RevisionStatus = 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED'

export type AccountStatus = 'PENDING_VERIFICATION' | 'ACTIVE' | 'BLOCKED'

export type CrefStatus = 'PENDING_REVIEW' | 'VERIFIED' | 'REJECTED'

export type ServiceMode = 'IN_PERSON' | 'HOME_VISIT' | 'ONLINE'

export type PriceUnit = 'PER_SESSION' | 'MONTHLY' | 'CONSULTATION'

export type AdminProfileDetail = {
  profileId: string
  slug: string
  status: ProfileStatus
  published: boolean
  publishedRevisionId: string | null
  publishedAt: string | null
  createdAt: string
  updatedAt: string
  account: {
    userId: string
    fullName: string
    email: string
    phoneNumber: string
    status: AccountStatus
  }
  revision: {
    revisionId: string
    versionNumber: number
    status: RevisionStatus
    requiresReview: boolean
    rejectionReason: string | null
    fullName: string
    biography: string
    whatsapp: string
    profileImageKey: string | null
    experienceStartedYear: number | null
    certifications: string | null
    gymsDescription: string | null
    startingPriceCents: number | null
    priceUnit: PriceUnit | null
    cref: {
      id: string
      registrationCode: string
      documentImageKey: string | null
      status: CrefStatus
      rejectionReason: string | null
      verifiedAt: string | null
    } | null
    modalities: Array<{
      id: number
      name: string
      slug: string
      active: boolean
    }>
    serviceModes: ServiceMode[]
    serviceAreas: Array<{
      id: string
      city: string
      stateCode: string
      neighborhood: string | null
      description: string | null
    }>
    submittedAt: string | null
    reviewedAt: string | null
    reviewedByUserId: string | null
    createdAt: string
    updatedAt: string
  }
}
