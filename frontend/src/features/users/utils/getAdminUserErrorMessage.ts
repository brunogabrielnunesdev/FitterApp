import { isAxiosError } from 'axios'

import type { ApiProblem } from '../../auth/types/auth'

export function getAdminUserErrorMessage(error: unknown, context: 'list' | 'detail') {
  if (!isAxiosError<ApiProblem>(error)) {
    return context === 'list'
      ? 'Não foi possível carregar os usuários. Tente novamente.'
      : 'Não foi possível carregar os dados desta conta. Tente novamente.'
  }
  if (!error.response) {
    return 'Não foi possível conectar à API. Confira sua conexão e tente novamente.'
  }
  if (error.response.status === 403) {
    return 'Sua conta não tem permissão para consultar usuários.'
  }
  if (error.response.data?.code === 'USER_NOT_FOUND') {
    return 'Esta conta não foi encontrada. Volte à lista e atualize os dados.'
  }
  return context === 'list'
    ? 'Não foi possível carregar os usuários. Tente novamente.'
    : 'Não foi possível carregar os dados desta conta. Tente novamente.'
}
