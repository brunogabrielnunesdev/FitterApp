import { isAxiosError } from 'axios';

import { ApiProblem } from '@/features/auth/types/auth';

const messages: Record<string, string> = {
  INVALID_CREDENTIALS: 'E-mail ou senha incorretos.',
  EMAIL_NOT_VERIFIED: 'Confirme seu e-mail antes de entrar.',
  ACCOUNT_BLOCKED: 'Esta conta está bloqueada. Entre em contato com o suporte.',
};

export function getLoginErrorMessage(error: unknown) {
  if (!isAxiosError<ApiProblem>(error)) {
    return 'Não foi possível entrar. Tente novamente.';
  }

  if (!error.response) {
    return 'Não foi possível conectar à API. Confira se ela está em execução.';
  }

  return messages[error.response.data?.code ?? ''] ?? 'Não foi possível entrar. Tente novamente.';
}
