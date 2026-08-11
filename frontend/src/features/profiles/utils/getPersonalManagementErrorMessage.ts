import { isAxiosError } from 'axios'

import type { ApiProblem } from '../../auth/types/auth'

const messages: Record<string, string> = {
  EMAIL_ALREADY_REGISTERED: 'Já existe uma conta cadastrada com este e-mail.',
  PROFILE_CONFLICT: 'O CREF informado já está vinculado a outro perfil.',
  PROFILE_INVALID_STATE: 'Esta revisão não pode mais ser editada. Atualize o perfil.',
  PROFILE_VALIDATION_ERROR: 'Revise os dados profissionais e tente novamente.',
  VALIDATION_ERROR: 'Alguns campos não atendem às regras da API. Revise o formulário.',
  PROFILE_NOT_FOUND: 'Este perfil não foi encontrado.',
}

export function getPersonalManagementErrorMessage(error: unknown) {
  if (!isAxiosError<ApiProblem>(error)) {
    return 'Não foi possível salvar o personal. Tente novamente.'
  }
  if (!error.response) {
    return 'Não foi possível conectar à API. Confira sua conexão e tente novamente.'
  }
  if (error.response.status === 403) {
    return 'Sua conta não tem permissão para cadastrar ou editar personais.'
  }
  return messages[error.response.data?.code ?? ''] ?? 'Não foi possível salvar o personal. Tente novamente.'
}
