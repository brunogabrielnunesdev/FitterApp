import { create } from 'axios';
import { Platform } from 'react-native';

import { getSession } from '@/features/auth/services/sessionStorage';

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
