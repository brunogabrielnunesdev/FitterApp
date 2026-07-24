import { api } from '@/common/services/api';
import { LoginRequest, LoginResponse } from '@/features/auth/types/auth';

export async function login(request: LoginRequest) {
  const { data } = await api.post<LoginResponse>('/api/v1/auth/login', request);
  return data;
}
