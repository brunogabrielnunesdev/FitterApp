import { createContext, PropsWithChildren, useContext, useEffect, useMemo, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';

import {
  clearSession,
  getSession,
  saveSession,
  subscribeToSession,
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
  const queryClient = useQueryClient();
  const [session, setSession] = useState<StoredSession | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = subscribeToSession((nextSession) => {
      setSession(nextSession);
      if (!nextSession) queryClient.clear();
    });

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

    return unsubscribe;
  }, [queryClient]);

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
