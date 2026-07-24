import { isAxiosError } from 'axios'

import type { ApiProblem } from '../types/auth'

const messages: Record<string, string> = {
  INVALID_CREDENTIALS: 'E-mail ou senha incorretos.',
  EMAIL_NOT_VERIFIED: 'O e-mail desta conta ainda não foi confirmado.',
  ACCOUNT_BLOCKED: 'Esta conta está bloqueada.',
}

export function getLoginErrorMessage(error: unknown) {
  if (!isAxiosError<ApiProblem>(error)) {
    return 'Não foi possível entrar. Tente novamente.'
  }

  if (!error.response) {
    return 'Não foi possível conectar à API. Confira se ela está em execução.'
  }

  return messages[error.response.data?.code ?? ''] ?? 'Não foi possível entrar. Tente novamente.'
}
