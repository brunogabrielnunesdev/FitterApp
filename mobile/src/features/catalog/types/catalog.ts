export type ServiceMode = 'IN_PERSON' | 'HOME_VISIT' | 'ONLINE';
export type PriceUnit = 'PER_SESSION' | 'MONTHLY' | 'CONSULTATION';

export type PublicProfileCard = {
  profileId: string;
  slug: string;
  fullName: string;
  biography: string | null;
  profileImageKey: string | null;
  startingPriceCents: number | null;
  priceUnit: PriceUnit | null;
};

export type PublicProfilePage = {
  content: PublicProfileCard[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PublicProfileDetail = PublicProfileCard & {
  experienceStartedYear: number | null;
  certifications: string | null;
  gymsDescription: string | null;
  modalities: { id: number; name: string; slug: string }[];
  serviceModes: ServiceMode[];
  serviceAreas: {
    city: string;
    stateCode: string;
    neighborhood: string | null;
    description: string | null;
  }[];
};

export type WhatsappContact = { whatsappUrl: string };

export type PublicProfilesQuery = {
  page: number;
  size: number;
  query?: string;
  modalityId?: number;
  neighborhood?: string;
  serviceMode?: ServiceMode;
  idempotencyKey: string;
};
