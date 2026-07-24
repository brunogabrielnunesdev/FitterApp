import { AxiosError } from 'axios';

import { ApiProblem } from '@/features/auth/types/auth';

const messages: Record<string, string> = {
  EMAIL_ALREADY_REGISTERED: 'Este e-mail já possui uma conta.',
  INVALID_VERIFICATION_TOKEN: 'O link de confirmação é inválido.',
  VERIFICATION_TOKEN_EXPIRED: 'O link expirou. Solicite um novo e-mail.',
  VERIFICATION_CONFLICT: 'Este e-mail já foi confirmado.',
  VALIDATION_ERROR: 'Revise os dados informados.',
};

export function getAuthErrorMessage(error: unknown) {
  if (error instanceof AxiosError) {
    const problem = error.response?.data as ApiProblem | undefined;
    if (problem?.code && messages[problem.code]) return messages[problem.code];
    if (!error.response) return 'Não foi possível conectar à API.';
  }
  return 'Não foi possível concluir. Tente novamente.';
}
