import { AxiosError, create } from 'axios'

import { clearSession, getAccessToken, getRefreshToken, saveSession } from '../../features/auth/services/sessionStorage'
import type { LoginResponse } from '../../features/auth/types/auth'

export const api = create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const accessToken = getAccessToken()
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

let refreshPromise: Promise<LoginResponse> | null = null

api.interceptors.response.use(undefined, async (error: AxiosError) => {
  const original = error.config as (typeof error.config & { _retry?: boolean }) | undefined
  if (error.response?.status !== 401 || !original || original._retry || original.url?.includes('/api/v1/auth/')) {
    return Promise.reject(error)
  }
  const refreshToken = getRefreshToken()
  if (!refreshToken) return Promise.reject(error)
  original._retry = true
  try {
    refreshPromise ??= api
      .post<LoginResponse>('/api/v1/auth/refresh', { refreshToken })
      .then((response) => response.data)
      .finally(() => { refreshPromise = null })
    const refreshed = await refreshPromise
    saveSession(refreshed)
    original.headers.Authorization = `Bearer ${refreshed.accessToken}`
    return api(original)
  } catch (refreshError) {
    clearSession()
    return Promise.reject(refreshError)
  }
})
