import { createContext, PropsWithChildren, useContext, useEffect, useMemo, useState } from 'react';

import {
  clearSession,
  getSession,
  saveSession,
} from '@/features/auth/services/sessionStorage';
import { LoginResponse, StoredSession } from '@/features/auth/types/auth';
import { logout, refreshSession } from '@/features/auth/services/authService';

type AuthContextValue = {
  session: StoredSession | null;
  isLoading: boolean;
  startSession: (response: LoginResponse) => Promise<void>;
  endSession: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [session, setSession] = useState<StoredSession | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getSession()
      .then(async (stored) => {
        if (!stored) return setSession(null);
        if (stored.expiresAt > Date.now() + 30_000) return setSession(stored);
        try {
          setSession(await saveSession(await refreshSession(stored.refreshToken)));
        } catch {
          await clearSession();
          setSession(null);
        }
      })
      .finally(() => setIsLoading(false));
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      isLoading,
      startSession: async (response) => setSession(await saveSession(response)),
      endSession: async () => {
        if (session?.refreshToken) {
          try { await logout(session.refreshToken); } catch { /* logout local continua */ }
        }
        await clearSession();
        setSession(null);
      },
    }),
    [isLoading, session],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
