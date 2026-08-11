import type { PriceUnit, ServiceMode } from './profileDetail'

export type PersonalProfileInput = {
  fullName: string
  biography: string | null
  whatsapp: string | null
  experienceStartedYear: number | null
  certifications: string | null
  gymsDescription: string | null
  startingPriceCents: number | null
  priceUnit: PriceUnit | null
  modalityIds: number[]
  serviceModes: ServiceMode[]
  serviceAreas: Array<{
    city: string
    stateCode: string
    neighborhood: string | null
    description: string | null
  }>
  cref: {
    registrationCode: string
    documentImageKey: string
  } | null
}

export type CreatePersonalRequest = {
  accountFullName: string
  email: string
  phoneNumber: string
  temporaryPassword: string
  profile: PersonalProfileInput
  reason: string
}

export type UpdatePersonalRequest = {
  profile: PersonalProfileInput
  reason: string
}

export type PersonalActionResponse = {
  userId: string
  profileId: string
  revisionId: string
}

export type PersonalFormValues = {
  accountFullName: string
  email: string
  phoneNumber: string
  temporaryPassword: string
  temporaryPasswordConfirmation: string
  fullName: string
  biography: string
  whatsapp: string
  experienceStartedYear: string
  certifications: string
  gymsDescription: string
  startingPrice: string
  priceUnit: '' | PriceUnit
  modalityIds: number[]
  serviceModes: ServiceMode[]
  serviceAreas: Array<{
    city: string
    stateCode: string
    neighborhood: string
    description: string
  }>
  crefRegistrationCode: string
  crefDocumentImageKey: string
  reason: string
}
