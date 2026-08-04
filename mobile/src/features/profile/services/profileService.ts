import { api } from '@/common/services/api';

export type ServiceMode = 'IN_PERSON' | 'HOME_VISIT' | 'ONLINE';

export type ServiceArea = {
  city: string;
  stateCode: string;
  neighborhood: string | null;
  description: string | null;
};

export type ProfileDraft = {
  profileId: string;
  revisionId: string;
  profileStatus: string;
  revisionStatus: string;
  rejectionReason: string | null;
  fullName: string | null;
  biography: string | null;
  whatsapp: string | null;
  experienceStartedYear: number | null;
  certifications: string | null;
  gymsDescription: string | null;
  startingPriceCents: number | null;
  priceUnit: 'PER_SESSION' | 'PER_MONTH' | null;
  crefRegistrationCode: string | null;
  crefDocumentImageKey: string | null;
  crefStatus?: string | null;
  modalityIds: number[];
  serviceModes: ServiceMode[];
  serviceAreas: ServiceArea[];
};

export type Modality = { id: number; name: string; slug: string };

export type ProfileStatus = 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'PUBLISHED' | 'REJECTED' | 'SUSPENDED';

export type OwnProfileStatus = {
  profileId: string;
  fullName: string | null;
  profileStatus: ProfileStatus;
  revisionStatus: string | null;
  rejectionReason: string | null;
  published: boolean;
  publishedRevisionId: string | null;
};

export async function getOwnProfile() {
  return (await api.get<OwnProfileStatus>('/api/v1/me/personal-profile')).data;
}

export async function getProfileDraft() {
  return (await api.get<ProfileDraft>('/api/v1/me/personal-profile/draft')).data;
}

export async function createPersonalProfile() {
  return (await api.post('/api/v1/me/personal-profile', {})).data;
}

export async function listModalities() {
  return (await api.get<Modality[]>('/api/v1/public/modalities')).data;
}

export async function updateProfileDraft(profileId: string, data: Partial<ProfileDraft>) {
  await api.put(`/api/v1/me/personal-profile/${profileId}`, data);
}

export async function updateProfileModalities(profileId: string, modalityIds: number[]) {
  await api.put(`/api/v1/me/personal-profile/${profileId}/modalities`, { modalityIds });
}

export async function updateCref(
  profileId: string,
  registrationCode: string,
  documentImageKey: string,
) {
  await api.put(`/api/v1/me/personal-profile/${profileId}/cref`, {
    registrationCode,
    documentImageKey,
  });
}

export async function updateServiceModes(profileId: string, serviceModes: ServiceMode[]) {
  await api.put(`/api/v1/me/personal-profile/${profileId}/service-modes`, { serviceModes });
}

export async function updateServiceAreas(profileId: string, serviceAreas: ServiceArea[]) {
  await api.put(`/api/v1/me/personal-profile/${profileId}/service-areas`, { serviceAreas });
}

export async function submitProfile(profileId: string) {
  await api.post(`/api/v1/me/personal-profile/${profileId}/submission`);
}

export async function publishProfile(profileId: string) {
  await api.post(`/api/v1/me/personal-profile/${profileId}/publication`);
}

export async function unpublishProfile(profileId: string) {
  await api.delete(`/api/v1/me/personal-profile/${profileId}/publication`);
}

export async function startProfileRevision(profileId: string) {
  await api.post(`/api/v1/me/personal-profile/${profileId}/revisions`);
}
