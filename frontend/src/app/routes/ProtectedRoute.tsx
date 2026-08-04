import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { useAuth } from '../../features/auth/context/useAuth'

export function ProtectedRoute() {
  const { isAdmin, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) return null

  if (!isAdmin) {
    return <Navigate replace state={{ from: location.pathname }} to="/login" />
  }

  return <Outlet />
}
