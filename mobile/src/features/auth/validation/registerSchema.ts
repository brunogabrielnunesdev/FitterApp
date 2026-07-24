import { z } from 'zod';

export const registerSchema = z
  .object({
    fullName: z.string().trim().min(3, 'Digite seu nome completo').max(120),
    email: z.email('Digite um e-mail válido').transform((email) => email.toLowerCase()),
    phoneNumber: z
      .string()
      .regex(/^\+[1-9][0-9]{7,14}$/, 'Use o formato internacional, exemplo: +5544999999999'),
    password: z.string().min(8, 'A senha precisa ter pelo menos 8 caracteres').max(72),
    passwordConfirmation: z.string(),
  })
  .refine((data) => data.password === data.passwordConfirmation, {
    message: 'As senhas não coincidem',
    path: ['passwordConfirmation'],
  });

export type RegisterForm = z.infer<typeof registerSchema>;
