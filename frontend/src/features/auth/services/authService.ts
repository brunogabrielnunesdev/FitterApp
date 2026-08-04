import { api } from '../../../common/services/api'
import type { LoginRequest, LoginResponse } from '../types/auth'

export async function login(request: LoginRequest) {
  const { data } = await api.post<LoginResponse>('/api/v1/auth/login', request)
  return data
}

export async function refreshSession(refreshToken: string) {
  const { data } = await api.post<LoginResponse>('/api/v1/auth/refresh', { refreshToken })
  return data
}

export async function logout(refreshToken: string) {
  await api.post('/api/v1/auth/logout', { refreshToken })
}

export async function requestPasswordReset(email: string) {
  await api.post('/api/v1/auth/password/forgot', { email })
}

export async function resetPassword(token: string, newPassword: string) {
  await api.post('/api/v1/auth/password/reset', { token, newPassword })
}
