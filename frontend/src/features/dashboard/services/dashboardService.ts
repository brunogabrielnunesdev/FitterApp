import { api } from '../../../common/services/api'
import type { AdminDashboard, DashboardRange } from '../types/dashboard'

export async function getAdminDashboard(range: DashboardRange, timezone: string) {
  const response = await api.get<AdminDashboard>('/api/v1/admin/dashboard/funnel', {
    params: { ...range, timezone },
  })
  return response.data
}
