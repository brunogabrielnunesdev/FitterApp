import { createContext } from 'react'

import type { LoginResponse } from '../types/auth'

export type AuthContextValue = {
  email: string | null
  isAdmin: boolean
  startSession: (session: LoginResponse) => boolean
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
