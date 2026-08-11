import { z } from 'zod'

const phonePattern = /^\+[1-9][0-9]{7,14}$/
const currentYear = new Date().getFullYear()

const serviceAreaSchema = z.object({
  city: z.string().trim().min(1, 'Informe a cidade').max(100, 'Use no máximo 100 caracteres'),
  stateCode: z
    .string()
    .trim()
    .regex(/^[A-Za-z]{2}$/, 'Informe a UF com duas letras'),
  neighborhood: z.string().trim().max(100, 'Use no máximo 100 caracteres'),
  description: z.string().trim().max(255, 'Use no máximo 255 caracteres'),
})

export function createPersonalManagementSchema(isCreate: boolean, hasExistingCref: boolean) {
  return z
    .object({
      accountFullName: z.string().trim().max(120, 'Use no máximo 120 caracteres'),
      email: z.string().trim().max(254, 'Use no máximo 254 caracteres'),
      phoneNumber: z.string().trim(),
      temporaryPassword: z.string().max(72, 'Use no máximo 72 caracteres'),
      temporaryPasswordConfirmation: z.string(),
      fullName: z.string().trim().min(1, 'Informe o nome profissional').max(120),
      biography: z.string().trim().max(1500, 'Use no máximo 1.500 caracteres'),
      whatsapp: z
        .string()
        .trim()
        .refine((value) => !value || phonePattern.test(value), 'Use o formato internacional, como +5544999999999'),
      experienceStartedYear: z
        .string()
        .trim()
        .refine(
          (value) => !value || (/^\d{4}$/.test(value) && Number(value) >= 1900 && Number(value) <= currentYear),
          `Informe um ano entre 1900 e ${currentYear}`,
        ),
      certifications: z.string().trim().max(1000, 'Use no máximo 1.000 caracteres'),
      gymsDescription: z.string().trim().max(500, 'Use no máximo 500 caracteres'),
      startingPrice: z
        .string()
        .trim()
        .refine((value) => !value || (!Number.isNaN(Number(value)) && Number(value) >= 0), 'Informe um valor válido'),
      priceUnit: z.enum(['', 'PER_SESSION', 'MONTHLY', 'CONSULTATION']),
      modalityIds: z.array(z.number()),
      serviceModes: z.array(z.enum(['IN_PERSON', 'HOME_VISIT', 'ONLINE'])),
      serviceAreas: z.array(serviceAreaSchema),
      crefRegistrationCode: z.string().trim().max(40, 'Use no máximo 40 caracteres'),
      crefDocumentImageKey: z.string().trim().max(255, 'Use no máximo 255 caracteres'),
      reason: z.string().trim().min(1, 'Informe a justificativa administrativa').max(1500),
    })
    .superRefine((values, context) => {
      if (isCreate) {
        if (!values.accountFullName) {
          context.addIssue({ code: 'custom', message: 'Informe o nome da conta', path: ['accountFullName'] })
        }
        if (!z.email().safeParse(values.email).success) {
          context.addIssue({ code: 'custom', message: 'Informe um e-mail válido', path: ['email'] })
        }
        if (!phonePattern.test(values.phoneNumber)) {
          context.addIssue({ code: 'custom', message: 'Use o formato internacional, como +5544999999999', path: ['phoneNumber'] })
        }
        if (values.temporaryPassword.length < 8) {
          context.addIssue({ code: 'custom', message: 'Use pelo menos 8 caracteres', path: ['temporaryPassword'] })
        }
        if (values.temporaryPassword !== values.temporaryPasswordConfirmation) {
          context.addIssue({ code: 'custom', message: 'As senhas não coincidem', path: ['temporaryPasswordConfirmation'] })
        }
      }

      const hasPrice = Boolean(values.startingPrice)
      const hasPriceUnit = Boolean(values.priceUnit)
      if (hasPrice !== hasPriceUnit) {
        context.addIssue({
          code: 'custom',
          message: 'Informe o valor e a unidade de preço juntos',
          path: [hasPrice ? 'priceUnit' : 'startingPrice'],
        })
      }

      const hasCrefCode = Boolean(values.crefRegistrationCode)
      const hasCrefDocument = Boolean(values.crefDocumentImageKey)
      if (hasExistingCref && !hasCrefCode && !hasCrefDocument) {
        context.addIssue({
          code: 'custom',
          message: 'O contrato atual não permite remover um CREF existente',
          path: ['crefRegistrationCode'],
        })
      }
      if (hasCrefCode !== hasCrefDocument) {
        context.addIssue({
          code: 'custom',
          message: 'Informe o número e a chave do documento juntos',
          path: [hasCrefCode ? 'crefDocumentImageKey' : 'crefRegistrationCode'],
        })
      }

      const locations = new Set<string>()
      values.serviceAreas.forEach((area, index) => {
        const key = `${area.stateCode}|${area.city}|${area.neighborhood}`.toLocaleLowerCase(
          'pt-BR',
        )
        if (locations.has(key)) {
          context.addIssue({
            code: 'custom',
            message: 'Esta região já foi adicionada',
            path: ['serviceAreas', index, 'city'],
          })
        }
        locations.add(key)
      })
    })
}
