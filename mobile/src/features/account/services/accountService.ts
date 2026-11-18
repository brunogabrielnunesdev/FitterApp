import { api } from '@/common/services/api';

export type Account = {
  userId: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  status: 'PENDING_VERIFICATION' | 'ACTIVE' | 'BLOCKED';
  roles: string[];
};

export async function getOwnAccount() {
  return (await api.get<Account>('/api/v1/me')).data;
}

export async function updateOwnAccount(fullName: string, phoneNumber: string) {
  return (await api.put<Account>('/api/v1/me', { fullName, phoneNumber })).data;
}
