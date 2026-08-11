import { isAxiosError } from 'axios'

import type { ApiProblem } from '../../auth/types/auth'

const messages: Record<string, string> = {
  MODALITY_ALREADY_EXISTS: 'Já existe uma modalidade com este nome ou identificador.',
  MODALITY_NOT_FOUND: 'Esta modalidade não foi encontrada. Atualize a lista.',
  INVALID_MODALITY_NAME: 'Informe um nome válido para a modalidade.',
  VALIDATION_ERROR: 'O nome deve ter entre 1 e 80 caracteres.',
}

export function getModalityErrorMessage(error: unknown, action: 'listar' | 'criar' | 'editar' | 'alterar') {
  if (!isAxiosError<ApiProblem>(error)) {
    return `Não foi possível ${action} a modalidade. Tente novamente.`
  }
  if (!error.response) {
    return 'Não foi possível conectar à API. Confira sua conexão e tente novamente.'
  }
  if (error.response.status === 403) {
    return 'Sua conta não tem permissão para gerenciar modalidades.'
  }
  return messages[error.response.data?.code ?? ''] ?? `Não foi possível ${action} a modalidade. Tente novamente.`
}
