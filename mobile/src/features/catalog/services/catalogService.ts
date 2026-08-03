import { api } from '@/common/services/api';
import {
  PublicProfileCard,
  PublicProfilePage,
  PublicProfilesQuery,
} from '@/features/catalog/types/catalog';

export async function listPublicProfiles({
  page,
  size,
  query,
  serviceMode,
}: PublicProfilesQuery) {
  const { data } = await api.get<PublicProfilePage>('/api/v1/public/personals', {
    params: { page, size, query: query || undefined, serviceMode },
  });
  return data;
}

export async function getPublicProfile(slug: string) {
  const { data } = await api.get<PublicProfileCard>(`/api/v1/public/personals/${slug}`);
  return data;
}
