import { isAxiosError } from 'axios'

import type { ApiProblem } from '../../auth/types/auth'

export function getDashboardErrorMessage(error: unknown) {
  if (!isAxiosError<ApiProblem>(error)) {
    return 'Não foi possível carregar as métricas. Tente novamente.'
  }
  if (!error.response) {
    return 'Não foi possível conectar à API de métricas. Confira sua conexão.'
  }
  if (error.response.status === 403) {
    return 'Sua conta não tem permissão para consultar as métricas administrativas.'
  }
  if (error.response.data?.code === 'INVALID_DASHBOARD_PERIOD') {
    return 'O período ou timezone informado não é válido.'
  }
  return 'Não foi possível carregar as métricas. Tente novamente.'
}
