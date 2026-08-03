export type ServiceMode = 'IN_PERSON' | 'HOME_VISIT' | 'ONLINE';

export type PublicProfileCard = {
  profileId: string;
  slug: string;
  fullName: string;
  biography: string | null;
  profileImageKey: string | null;
  startingPriceCents: number | null;
  priceUnit: 'PER_SESSION' | 'PER_MONTH' | null;
};

export type PublicProfilePage = {
  content: PublicProfileCard[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PublicProfilesQuery = {
  page: number;
  size: number;
  query?: string;
  serviceMode?: ServiceMode;
};
