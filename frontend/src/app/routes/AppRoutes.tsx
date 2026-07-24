import { Navigate, Route, Routes } from 'react-router-dom'

import { ProtectedRoute } from './ProtectedRoute'
import { AdminLoginPage } from '../../features/auth/pages/AdminLoginPage'
import { AdminDashboardPage } from '../../features/dashboard/pages/AdminDashboardPage'

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<AdminLoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/admin" element={<AdminDashboardPage />} />
      </Route>
      <Route path="*" element={<Navigate replace to="/admin" />} />
    </Routes>
  )
}
