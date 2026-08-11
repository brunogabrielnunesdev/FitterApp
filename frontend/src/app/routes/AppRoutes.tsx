import { Navigate, Route, Routes } from 'react-router-dom'

import { ProtectedRoute } from './ProtectedRoute'
import { AdminLoginPage } from '../../features/auth/pages/AdminLoginPage'
import { AdminDashboardPage } from '../../features/dashboard/pages/AdminDashboardPage'
import { PendingProfilesPage } from '../../features/profiles/pages/PendingProfilesPage'
import { PasswordRecoveryPage } from '../../features/auth/pages/PasswordRecoveryPage'

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<AdminLoginPage />} />
      <Route path="/forgot-password" element={<PasswordRecoveryPage />} />
      <Route path="/reset-password" element={<PasswordRecoveryPage reset />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/admin" element={<AdminDashboardPage />} />
        <Route path="/admin/personals/pending" element={<PendingProfilesPage />} />
      </Route>
      <Route path="*" element={<Navigate replace to="/admin" />} />
    </Routes>
  )
}
