import { createContext, PropsWithChildren, useContext, useEffect, useMemo, useState } from 'react';

import {
  clearSession,
  getSession,
  saveSession,
} from '@/features/auth/services/sessionStorage';
import { LoginResponse, StoredSession } from '@/features/auth/types/auth';

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
      .then(setSession)
      .finally(() => setIsLoading(false));
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      isLoading,
      startSession: async (response) => setSession(await saveSession(response)),
      endSession: async () => {
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
