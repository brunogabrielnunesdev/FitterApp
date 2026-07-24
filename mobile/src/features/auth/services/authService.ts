import { api } from '@/common/services/api';
import {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from '@/features/auth/types/auth';

export async function login(request: LoginRequest) {
  const { data } = await api.post<LoginResponse>('/api/v1/auth/login', request);
  return data;
}

export async function register(request: RegisterRequest) {
  const { data } = await api.post<RegisterResponse>('/api/v1/auth/register', request);
  return data;
}

export async function confirmEmail(token: string) {
  await api.post('/api/v1/auth/email/confirm', { token });
}

export async function resendConfirmation(email: string) {
  await api.post('/api/v1/auth/email/resend', { email });
}
