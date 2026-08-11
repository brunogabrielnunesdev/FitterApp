import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'

import { ProtectedRoute } from './ProtectedRoute'
import { AdminLayout } from '../layout/AdminLayout'
import { AdminLoginPage } from '../../features/auth/pages/AdminLoginPage'
import { AdminDashboardPage } from '../../features/dashboard/pages/AdminDashboardPage'
import { AdminUserDetailPage } from '../../features/users/pages/AdminUserDetailPage'
import { AdminUsersPage } from '../../features/users/pages/AdminUsersPage'
import { PendingProfilesPage } from '../../features/profiles/pages/PendingProfilesPage'
import { AdminProfilesPage } from '../../features/profiles/pages/AdminProfilesPage'
import { ProfileDetailPage } from '../../features/profiles/pages/ProfileDetailPage'
import { PasswordRecoveryPage } from '../../features/auth/pages/PasswordRecoveryPage'

const CreatePersonalPage = lazy(
  () => import('../../features/profiles/pages/CreatePersonalPage'),
)
const EditPersonalPage = lazy(
  () => import('../../features/profiles/pages/EditPersonalPage'),
)
const AdminModalitiesPage = lazy(
  () => import('../../features/modalities/pages/AdminModalitiesPage'),
)

function AdminPageLoading() {
  return (
    <div className="mx-auto max-w-5xl px-6 py-14" role="status">
      <span className="sr-only">Carregando página</span>
      <div className="h-12 max-w-lg animate-pulse rounded-xl bg-[#202020]" />
      <div className="mt-8 h-72 animate-pulse rounded-[26px] border border-[#292929] bg-[#111]" />
    </div>
  )
}

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
          <Route
            path="/admin/personals/new"
            element={
              <Suspense fallback={<AdminPageLoading />}>
                <CreatePersonalPage />
              </Suspense>
            }
          />
          <Route
            path="/admin/personals/:profileId/edit"
            element={
              <Suspense fallback={<AdminPageLoading />}>
                <EditPersonalPage />
              </Suspense>
            }
          />
          <Route path="/admin/personals/pending" element={<PendingProfilesPage />} />
          <Route path="/admin/personals/:profileId" element={<ProfileDetailPage />} />
          <Route path="/admin/users" element={<AdminUsersPage />} />
          <Route path="/admin/users/:userId" element={<AdminUserDetailPage />} />
          <Route
            path="/admin/modalities"
            element={
              <Suspense fallback={<AdminPageLoading />}>
                <AdminModalitiesPage />
              </Suspense>
            }
          />
        </Route>
      </Route>
      <Route path="*" element={<Navigate replace to="/admin" />} />
    </Routes>
  )
}
