import { isAxiosError } from 'axios'

import type { ApiProblem } from '../../auth/types/auth'

const messages: Record<string, string> = {
  PROFILE_NOT_FOUND: 'Este perfil não foi encontrado. Atualize a fila e tente novamente.',
  PROFILE_INVALID_STATE: 'Este perfil não está mais aguardando análise. Atualize a fila.',
  PROFILE_VALIDATION_ERROR: 'Não foi possível validar esta ação. Revise os dados e tente novamente.',
}

export function getModerationErrorMessage(error: unknown, action: 'carregar' | 'aprovar' | 'reprovar') {
  if (!isAxiosError<ApiProblem>(error)) {
    return `Não foi possível ${action} o perfil. Tente novamente.`
  }

  if (!error.response) {
    return 'Não foi possível conectar à API. Confira sua conexão e tente novamente.'
  }

  if (error.response.status === 403) {
    return 'Sua conta não tem permissão para moderar perfis.'
  }

  return (
    messages[error.response.data?.code ?? ''] ??
    `Não foi possível ${action} o perfil. Tente novamente.`
  )
}
