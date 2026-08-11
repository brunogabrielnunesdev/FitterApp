import type { AdminProfileDetail } from '../types/profileDetail'
import type {
  CreatePersonalRequest,
  PersonalFormValues,
  PersonalProfileInput,
  UpdatePersonalRequest,
} from '../types/personalManagement'

function optional(value: string) {
  const normalized = value.trim()
  return normalized || null
}

function toProfileInput(values: PersonalFormValues): PersonalProfileInput {
  const hasCref = Boolean(values.crefRegistrationCode.trim())
  return {
    fullName: values.fullName.trim(),
    biography: optional(values.biography),
    whatsapp: optional(values.whatsapp),
    experienceStartedYear: values.experienceStartedYear
      ? Number(values.experienceStartedYear)
      : null,
    certifications: optional(values.certifications),
    gymsDescription: optional(values.gymsDescription),
    startingPriceCents: values.startingPrice
      ? Math.round(Number(values.startingPrice) * 100)
      : null,
    priceUnit: values.priceUnit || null,
    modalityIds: values.modalityIds,
    serviceModes: values.serviceModes,
    serviceAreas: values.serviceAreas.map((area) => ({
      city: area.city.trim(),
      stateCode: area.stateCode.trim().toUpperCase(),
      neighborhood: optional(area.neighborhood),
      description: optional(area.description),
    })),
    cref: hasCref
      ? {
          registrationCode: values.crefRegistrationCode.trim(),
          documentImageKey: values.crefDocumentImageKey.trim(),
        }
      : null,
  }
}

export function toCreatePersonalRequest(values: PersonalFormValues): CreatePersonalRequest {
  return {
    accountFullName: values.accountFullName.trim(),
    email: values.email.trim().toLocaleLowerCase('pt-BR'),
    phoneNumber: values.phoneNumber.trim(),
    temporaryPassword: values.temporaryPassword,
    profile: toProfileInput(values),
    reason: values.reason.trim(),
  }
}

export function toUpdatePersonalRequest(values: PersonalFormValues): UpdatePersonalRequest {
  return {
    profile: toProfileInput(values),
    reason: values.reason.trim(),
  }
}

export function emptyPersonalFormValues(): PersonalFormValues {
  return {
    accountFullName: '',
    email: '',
    phoneNumber: '',
    temporaryPassword: '',
    temporaryPasswordConfirmation: '',
    fullName: '',
    biography: '',
    whatsapp: '',
    experienceStartedYear: '',
    certifications: '',
    gymsDescription: '',
    startingPrice: '',
    priceUnit: '',
    modalityIds: [],
    serviceModes: [],
    serviceAreas: [],
    crefRegistrationCode: '',
    crefDocumentImageKey: '',
    reason: '',
  }
}

export function personalFormValuesFromDetail(detail: AdminProfileDetail): PersonalFormValues {
  const revision = detail.revision
  return {
    ...emptyPersonalFormValues(),
    fullName: revision.fullName,
    biography: revision.biography ?? '',
    whatsapp: revision.whatsapp ?? '',
    experienceStartedYear: revision.experienceStartedYear?.toString() ?? '',
    certifications: revision.certifications ?? '',
    gymsDescription: revision.gymsDescription ?? '',
    startingPrice:
      revision.startingPriceCents === null
        ? ''
        : (revision.startingPriceCents / 100).toFixed(2),
    priceUnit: revision.priceUnit ?? '',
    modalityIds: revision.modalities.map((modality) => modality.id),
    serviceModes: revision.serviceModes,
    serviceAreas: revision.serviceAreas.map((area) => ({
      city: area.city,
      stateCode: area.stateCode,
      neighborhood: area.neighborhood ?? '',
      description: area.description ?? '',
    })),
    crefRegistrationCode: revision.cref?.registrationCode ?? '',
    crefDocumentImageKey: revision.cref?.documentImageKey ?? '',
  }
}
