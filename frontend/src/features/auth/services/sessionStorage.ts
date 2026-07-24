import { jwtDecode } from 'jwt-decode'

import type { AccessTokenClaims, LoginResponse } from '../types/auth'

const ACCESS_TOKEN_KEY = 'fitterapp.admin.accessToken'
const REFRESH_TOKEN_KEY = 'fitterapp.admin.refreshToken'

export function saveSession(session: LoginResponse) {
  sessionStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken)
  sessionStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken)
}

export function clearSession() {
  sessionStorage.removeItem(ACCESS_TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
}

export function getAccessToken() {
  return sessionStorage.getItem(ACCESS_TOKEN_KEY)
}

export function getCurrentClaims() {
  const accessToken = getAccessToken()
  if (!accessToken) {
    return null
  }

  try {
    const claims = jwtDecode<AccessTokenClaims>(accessToken)
    if (claims.exp * 1_000 <= Date.now()) {
      clearSession()
      return null
    }
    return claims
  } catch {
    clearSession()
    return null
  }
}

export function hasAdminRole(accessToken: string) {
  try {
    return jwtDecode<AccessTokenClaims>(accessToken).roles?.includes('ADMIN') ?? false
  } catch {
    return false
  }
}
