import { useMemo, useState, type PropsWithChildren } from 'react'

import {
  clearSession,
  getCurrentClaims,
  hasAdminRole,
  saveSession,
} from '../services/sessionStorage'
import { AuthContext, type AuthContextValue } from './authContextDefinition'

export function AuthProvider({ children }: PropsWithChildren) {
  const [claims, setClaims] = useState(getCurrentClaims)

  const value = useMemo<AuthContextValue>(
    () => ({
      email: claims?.email ?? null,
      isAdmin: claims?.roles?.includes('ADMIN') ?? false,
      startSession: (session) => {
        if (!hasAdminRole(session.accessToken)) {
          clearSession()
          return false
        }
        saveSession(session)
        setClaims(getCurrentClaims())
        return true
      },
      logout: () => {
        clearSession()
        setClaims(null)
      },
    }),
    [claims],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
