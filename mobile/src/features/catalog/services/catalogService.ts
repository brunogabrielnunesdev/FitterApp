import { api } from '@/common/services/api';
import { getMetricHeaders, MOBILE_EVENT_SOURCE } from '@/common/services/metrics';
import {
  PublicProfileDetail,
  PublicProfilePage,
  PublicProfilesQuery,
  WhatsappContact,
} from '@/features/catalog/types/catalog';

export async function listPublicProfiles({
  page,
  size,
  query,
  modalityId,
  neighborhood,
  serviceMode,
  idempotencyKey,
}: PublicProfilesQuery) {
  const { data } = await api.get<PublicProfilePage>('/api/v1/public/personals', {
    params: {
      page,
      size,
      query: query || undefined,
      modalityId,
      neighborhood: neighborhood || undefined,
      serviceMode,
      source: MOBILE_EVENT_SOURCE,
    },
    headers: await getMetricHeaders(idempotencyKey),
  });
  return data;
}

export async function listActiveModalities() {
  const { data } = await api.get<{ id: number; name: string; slug: string }[]>(
    '/api/v1/public/modalities',
  );
  return data;
}

export async function getPublicProfile(slug: string, idempotencyKey: string) {
  const { data } = await api.get<PublicProfileDetail>(`/api/v1/public/personals/${slug}`, {
    params: { source: MOBILE_EVENT_SOURCE },
    headers: await getMetricHeaders(idempotencyKey),
  });
  return data;
}

export async function startWhatsappContact(slug: string, idempotencyKey: string) {
  const { data } = await api.post<WhatsappContact>(
    `/api/v1/public/personals/${slug}/contact/whatsapp`,
    undefined,
    {
      params: { source: MOBILE_EVENT_SOURCE },
      headers: await getMetricHeaders(idempotencyKey),
    },
  );
  return data;
}
