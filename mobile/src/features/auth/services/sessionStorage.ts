import * as SecureStore from 'expo-secure-store';

import { LoginResponse, StoredSession } from '@/features/auth/types/auth';

const SESSION_KEY = 'fitterapp.session';

export async function saveSession(session: LoginResponse) {
  const storedSession: StoredSession = {
    ...session,
    expiresAt: Date.now() + session.expiresInSeconds * 1000,
  };
  await SecureStore.setItemAsync(SESSION_KEY, JSON.stringify(storedSession));
  return storedSession;
}

export async function getSession(): Promise<StoredSession | null> {
  const serializedSession = await SecureStore.getItemAsync(SESSION_KEY);
  if (!serializedSession) return null;

  try {
    const session = JSON.parse(serializedSession) as StoredSession;
    if (!session.accessToken || session.expiresAt <= Date.now()) {
      await clearSession();
      return null;
    }
    return session;
  } catch {
    await clearSession();
    return null;
  }
}

export async function clearSession() {
  await SecureStore.deleteItemAsync(SESSION_KEY);
}
