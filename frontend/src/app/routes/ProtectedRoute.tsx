import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { useAuth } from '../../features/auth/context/useAuth'

export function ProtectedRoute() {
  const { isAdmin } = useAuth()
  const location = useLocation()

  if (!isAdmin) {
    return <Navigate replace state={{ from: location.pathname }} to="/login" />
  }

  return <Outlet />
}
