import { useEffect, useMemo, useState, type PropsWithChildren } from 'react'

import {
  clearSession,
  getCurrentClaims,
  getRefreshToken,
  hasAdminRole,
  saveSession,
} from '../services/sessionStorage'
import { logout as revokeSession, refreshSession } from '../services/authService'
import { AuthContext, type AuthContextValue } from './authContextDefinition'

export function AuthProvider({ children }: PropsWithChildren) {
  const [claims, setClaims] = useState(getCurrentClaims)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const refreshToken = getRefreshToken()
    if (getCurrentClaims() || !refreshToken) {
      setIsLoading(false)
      return
    }
    refreshSession(refreshToken)
      .then((session) => {
        if (!hasAdminRole(session.accessToken)) throw new Error('Admin role required')
        saveSession(session)
        setClaims(getCurrentClaims())
      })
      .catch(() => clearSession())
      .finally(() => setIsLoading(false))
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      email: claims?.email ?? null,
      isAdmin: claims?.roles?.includes('ADMIN') ?? false,
      isLoading,
      startSession: (session) => {
        if (!hasAdminRole(session.accessToken)) {
          clearSession()
          return false
        }
        saveSession(session)
        setClaims(getCurrentClaims())
        return true
      },
      logout: async () => {
        const refreshToken = getRefreshToken()
        if (refreshToken) {
          try { await revokeSession(refreshToken) } catch { /* mantém logout local */ }
        }
        clearSession()
        setClaims(null)
      },
    }),
    [claims, isLoading],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
