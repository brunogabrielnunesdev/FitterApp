import * as SecureStore from 'expo-secure-store';

import { LoginResponse } from '@/features/auth/types/auth';

const ACCESS_TOKEN_KEY = 'fitterapp.accessToken';
const REFRESH_TOKEN_KEY = 'fitterapp.refreshToken';

export async function saveSession(session: LoginResponse) {
  await Promise.all([
    SecureStore.setItemAsync(ACCESS_TOKEN_KEY, session.accessToken),
    SecureStore.setItemAsync(REFRESH_TOKEN_KEY, session.refreshToken),
  ]);
}

export async function clearSession() {
  await Promise.all([
    SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY),
    SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY),
  ]);
}
