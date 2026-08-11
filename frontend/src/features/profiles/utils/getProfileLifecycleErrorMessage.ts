import { isAxiosError } from 'axios'

import type { ApiProblem } from '../../auth/types/auth'
import type { ProfileLifecycleAction } from '../types/profileManagement'

export function getProfileLifecycleErrorMessage(
  error: unknown,
  action: ProfileLifecycleAction,
) {
  const verb = action === 'suspend' ? 'suspender' : 'reativar'
  if (!isAxiosError<ApiProblem>(error)) {
    return `Não foi possível ${verb} o perfil. Tente novamente.`
  }
  if (!error.response) {
    return 'Não foi possível conectar à API. Confira sua conexão e tente novamente.'
  }
  if (error.response.status === 403) {
    return 'Sua conta não tem permissão para alterar o estado deste perfil.'
  }
  if (error.response.data?.code === 'PROFILE_NOT_FOUND') {
    return 'Este perfil não foi encontrado. Volte à lista e atualize os dados.'
  }
  if (error.response.data?.code === 'PROFILE_MODERATION_INVALID_STATE') {
    return 'O estado do perfil mudou e esta ação não é mais permitida. Atualize os dados.'
  }
  if (error.response.data?.code === 'PROFILE_VALIDATION_ERROR') {
    return 'Informe uma justificativa válida para concluir a ação.'
  }
  return `Não foi possível ${verb} o perfil. Tente novamente.`
}
