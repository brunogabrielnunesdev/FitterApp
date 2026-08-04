import { AxiosError, create } from 'axios';
import { Platform } from 'react-native';

import { clearSession, getSession, saveSession } from '@/features/auth/services/sessionStorage';
import { LoginResponse } from '@/features/auth/types/auth';

const emulatorUrl =
  Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';

export const api = create({
  baseURL: process.env.EXPO_PUBLIC_API_URL ?? emulatorUrl,
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use(async (config) => {
  const session = await getSession();
  if (session) config.headers.Authorization = `${session.tokenType} ${session.accessToken}`;
  return config;
});

let refreshPromise: Promise<LoginResponse> | null = null;

api.interceptors.response.use(undefined, async (error: AxiosError) => {
  const original = error.config as (typeof error.config & { _retry?: boolean }) | undefined;
  if (error.response?.status !== 401 || !original || original._retry || original.url?.includes('/api/v1/auth/')) {
    return Promise.reject(error);
  }
  const session = await getSession();
  if (!session?.refreshToken) return Promise.reject(error);
  original._retry = true;
  try {
    refreshPromise ??= api
      .post<LoginResponse>('/api/v1/auth/refresh', { refreshToken: session.refreshToken })
      .then((response) => response.data)
      .finally(() => { refreshPromise = null; });
    const refreshed = await refreshPromise;
    await saveSession(refreshed);
    original.headers.Authorization = `${refreshed.tokenType} ${refreshed.accessToken}`;
    return api(original);
  } catch (refreshError) {
    await clearSession();
    return Promise.reject(refreshError);
  }
});
