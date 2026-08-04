import { createContext } from 'react'

import type { LoginResponse } from '../types/auth'

export type AuthContextValue = {
  email: string | null
  isAdmin: boolean
  isLoading: boolean
  startSession: (session: LoginResponse) => boolean
  logout: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
