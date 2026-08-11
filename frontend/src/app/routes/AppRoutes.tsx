import { Navigate, Route, Routes } from 'react-router-dom'

import { ProtectedRoute } from './ProtectedRoute'
import { AdminLayout } from '../layout/AdminLayout'
import { AdminSectionUnavailable } from '../../common/components/AdminSectionUnavailable'
import { AdminLoginPage } from '../../features/auth/pages/AdminLoginPage'
import { AdminDashboardPage } from '../../features/dashboard/pages/AdminDashboardPage'
import { PendingProfilesPage } from '../../features/profiles/pages/PendingProfilesPage'
import { AdminProfilesPage } from '../../features/profiles/pages/AdminProfilesPage'
import { ProfileDetailPage } from '../../features/profiles/pages/ProfileDetailPage'
import { PasswordRecoveryPage } from '../../features/auth/pages/PasswordRecoveryPage'

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<AdminLoginPage />} />
      <Route path="/forgot-password" element={<PasswordRecoveryPage />} />
      <Route path="/reset-password" element={<PasswordRecoveryPage reset />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AdminLayout />}>
          <Route path="/admin" element={<AdminDashboardPage />} />
          <Route path="/admin/personals" element={<AdminProfilesPage />} />
          <Route path="/admin/personals/pending" element={<PendingProfilesPage />} />
          <Route path="/admin/personals/:profileId" element={<ProfileDetailPage />} />
          <Route
            path="/admin/users"
            element={
              <AdminSectionUnavailable
                description="A listagem e o detalhe de contas serão conectados ao contrato administrativo na WEB-05."
                eyebrow="USUÁRIOS"
                title="Gestão de usuários"
              />
            }
          />
          <Route
            path="/admin/modalities"
            element={
              <AdminSectionUnavailable
                description="O catálogo, a edição e a ativação de modalidades serão conectados ao contrato administrativo na WEB-07."
                eyebrow="MODALIDADES"
                title="Catálogo de modalidades"
              />
            }
          />
        </Route>
      </Route>
      <Route path="*" element={<Navigate replace to="/admin" />} />
    </Routes>
  )
}
