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

export async function refreshSession(refreshToken: string) {
  const { data } = await api.post<LoginResponse>('/api/v1/auth/refresh', { refreshToken });
  return data;
}

export async function logout(refreshToken: string) {
  await api.post('/api/v1/auth/logout', { refreshToken });
}

export async function requestPasswordReset(email: string) {
  await api.post('/api/v1/auth/password/forgot', { email });
}

export async function resetPassword(token: string, newPassword: string) {
  await api.post('/api/v1/auth/password/reset', { token, newPassword });
}
