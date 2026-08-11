import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { useFieldArray, useForm, type FieldError } from 'react-hook-form'

import { listAdminModalities } from '../services/personalManagementService'
import type { PersonalFormValues } from '../types/personalManagement'
import { createPersonalManagementSchema } from '../validation/personalManagementSchema'

type PersonalManagementFormProps = {
  mode: 'create' | 'edit'
  defaultValues: PersonalFormValues
  isSubmitting: boolean
  onSubmit: (values: PersonalFormValues) => Promise<void>
}

const inputClassName =
  'mt-2 h-12 w-full rounded-xl border border-[#333] bg-[#171717] px-4 text-sm outline-none transition placeholder:text-[#666] focus:border-[#7657ff] disabled:opacity-55'
const textareaClassName =
  'mt-2 min-h-28 w-full resize-y rounded-xl border border-[#333] bg-[#171717] px-4 py-3 text-sm outline-none transition placeholder:text-[#666] focus:border-[#7657ff] disabled:opacity-55'

function ErrorText({ error }: { error?: FieldError }) {
  if (!error) return null
  return <p className="mt-1.5 text-xs text-[#ff8a8a]">{error.message}</p>
}

function SectionHeading({ eyebrow, title, description }: { eyebrow: string; title: string; description: string }) {
  return (
    <div className="border-b border-[#292929] pb-5">
      <p className="text-[11px] font-bold tracking-[.15em] text-[#7657ff]">{eyebrow}</p>
      <h2 className="mt-2 font-['Manrope'] text-xl font-extrabold">{title}</h2>
      <p className="mt-2 text-sm leading-6 text-[#888]">{description}</p>
    </div>
  )
}

export function PersonalManagementForm({
  mode,
  defaultValues,
  isSubmitting,
  onSubmit,
}: PersonalManagementFormProps) {
  const isCreate = mode === 'create'
  const [pendingValues, setPendingValues] = useState<PersonalFormValues>()
  const modalitiesQuery = useQuery({
    queryKey: ['admin-modalities', 'form-options'],
    queryFn: listAdminModalities,
    retry: false,
  })
  const {
    control,
    register,
    setError,
    watch,
    handleSubmit,
    formState: { errors },
  } = useForm<PersonalFormValues>({
    resolver: zodResolver(
      createPersonalManagementSchema(isCreate, Boolean(defaultValues.crefRegistrationCode)),
    ),
    defaultValues,
    mode: 'onBlur',
  })
  const serviceAreas = useFieldArray({ control, name: 'serviceAreas' })
  const selectedModalityIds = watch('modalityIds')

  async function confirmSubmit() {
    if (!pendingValues) return
    try {
      await onSubmit(pendingValues)
    } catch {
      setPendingValues(undefined)
    }
  }

  function reviewSubmission(values: PersonalFormValues) {
    const hasInactiveModality = values.modalityIds.some(
      (modalityId) =>
        modalitiesQuery.data?.some(
          (modality) => modality.id === modalityId && !modality.active,
        ),
    )
    if (hasInactiveModality) {
      setError('modalityIds', {
        message: 'Remova as modalidades inativas antes de salvar',
      })
      return
    }
    setPendingValues(values)
  }

  return (
    <>
      <form
        className="grid gap-6"
        noValidate
        onSubmit={handleSubmit(reviewSubmission)}>
        {isCreate && (
          <section className="rounded-[26px] border border-[#292929] bg-[#111] p-6 md:p-7">
            <SectionHeading
              description="A conta será criada com verificação de e-mail pendente e uma senha temporária."
              eyebrow="CONTA"
              title="Dados de acesso"
            />
            <div className="mt-6 grid gap-5 md:grid-cols-2">
              <div>
                <label className="text-sm font-bold" htmlFor="account-full-name">Nome completo</label>
                <input className={inputClassName} disabled={isSubmitting} id="account-full-name" {...register('accountFullName')} />
                <ErrorText error={errors.accountFullName} />
              </div>
              <div>
                <label className="text-sm font-bold" htmlFor="account-email">E-mail</label>
                <input className={inputClassName} disabled={isSubmitting} id="account-email" type="email" {...register('email')} />
                <ErrorText error={errors.email} />
              </div>
              <div>
                <label className="text-sm font-bold" htmlFor="account-phone">Telefone</label>
                <input className={inputClassName} disabled={isSubmitting} id="account-phone" placeholder="+5544999999999" {...register('phoneNumber')} />
                <ErrorText error={errors.phoneNumber} />
              </div>
              <div />
              <div>
                <label className="text-sm font-bold" htmlFor="temporary-password">Senha temporária</label>
                <input className={inputClassName} disabled={isSubmitting} id="temporary-password" type="password" {...register('temporaryPassword')} />
                <ErrorText error={errors.temporaryPassword} />
              </div>
              <div>
                <label className="text-sm font-bold" htmlFor="temporary-password-confirmation">Confirmar senha</label>
                <input className={inputClassName} disabled={isSubmitting} id="temporary-password-confirmation" type="password" {...register('temporaryPasswordConfirmation')} />
                <ErrorText error={errors.temporaryPasswordConfirmation} />
              </div>
            </div>
          </section>
        )}

        <section className="rounded-[26px] border border-[#292929] bg-[#111] p-6 md:p-7">
          <SectionHeading
            description="Informações exibidas na apresentação profissional e usadas durante a análise."
            eyebrow="PERFIL"
            title="Apresentação profissional"
          />
          <div className="mt-6 grid gap-5 md:grid-cols-2">
            <div className="md:col-span-2">
              <label className="text-sm font-bold" htmlFor="professional-name">Nome profissional</label>
              <input className={inputClassName} disabled={isSubmitting} id="professional-name" {...register('fullName')} />
              <ErrorText error={errors.fullName} />
            </div>
            <div className="md:col-span-2">
              <label className="text-sm font-bold" htmlFor="biography">Biografia</label>
              <textarea className={textareaClassName} disabled={isSubmitting} id="biography" {...register('biography')} />
              <ErrorText error={errors.biography} />
            </div>
            <div>
              <label className="text-sm font-bold" htmlFor="whatsapp">WhatsApp</label>
              <input className={inputClassName} disabled={isSubmitting} id="whatsapp" placeholder="+5544999999999" {...register('whatsapp')} />
              <ErrorText error={errors.whatsapp} />
            </div>
            <div>
              <label className="text-sm font-bold" htmlFor="experience-year">Ano de início da experiência</label>
              <input className={inputClassName} disabled={isSubmitting} id="experience-year" inputMode="numeric" placeholder="2020" {...register('experienceStartedYear')} />
              <ErrorText error={errors.experienceStartedYear} />
            </div>
            <div>
              <label className="text-sm font-bold" htmlFor="certifications">Certificações</label>
              <textarea className={textareaClassName} disabled={isSubmitting} id="certifications" {...register('certifications')} />
              <ErrorText error={errors.certifications} />
            </div>
            <div>
              <label className="text-sm font-bold" htmlFor="gyms-description">Academias e locais</label>
              <textarea className={textareaClassName} disabled={isSubmitting} id="gyms-description" {...register('gymsDescription')} />
              <ErrorText error={errors.gymsDescription} />
            </div>
            <div>
              <label className="text-sm font-bold" htmlFor="starting-price">Preço inicial (R$)</label>
              <input className={inputClassName} disabled={isSubmitting} id="starting-price" min="0" step="0.01" type="number" {...register('startingPrice')} />
              <ErrorText error={errors.startingPrice} />
            </div>
            <div>
              <label className="text-sm font-bold" htmlFor="price-unit">Unidade do preço</label>
              <select className={inputClassName} disabled={isSubmitting} id="price-unit" {...register('priceUnit')}>
                <option value="">Não informado</option>
                <option value="PER_SESSION">Por sessão</option>
                <option value="MONTHLY">Por mês</option>
                <option value="CONSULTATION">Por consulta</option>
              </select>
              <ErrorText error={errors.priceUnit} />
            </div>
          </div>
        </section>

        <section className="rounded-[26px] border border-[#292929] bg-[#111] p-6 md:p-7">
          <SectionHeading
            description="Selecione especialidades ativas e os formatos oferecidos pelo personal."
            eyebrow="SERVIÇOS"
            title="Modalidades e atendimento"
          />
          <div className="mt-6 grid gap-7 md:grid-cols-2">
            <fieldset>
              <legend className="text-sm font-bold">Modalidades</legend>
              {modalitiesQuery.isPending && <p className="mt-3 text-sm text-[#888]">Carregando modalidades…</p>}
              {modalitiesQuery.isError && (
                <div className="mt-3 rounded-xl border border-[#ff6b6b]/35 p-4 text-sm text-[#ff9b9b]">
                  Não foi possível carregar as modalidades.
                  <button className="ml-2 font-bold underline" onClick={() => void modalitiesQuery.refetch()} type="button">Tentar novamente</button>
                </div>
              )}
              {modalitiesQuery.isSuccess && (
                <div className="mt-3 grid gap-2 sm:grid-cols-2">
                  {modalitiesQuery.data
                    .filter(
                      (modality) =>
                        modality.active || selectedModalityIds.includes(modality.id),
                    )
                    .map((modality) => (
                    <label className="flex items-center gap-3 rounded-xl border border-[#333] bg-[#171717] p-3 text-sm" key={modality.id}>
                      <input disabled={isSubmitting} type="checkbox" value={modality.id} {...register('modalityIds', { valueAsNumber: true })} />
                      <span>
                        {modality.name}
                        {!modality.active && (
                          <span className="ml-1 text-xs text-[#ff9f43]">(inativa — remova)</span>
                        )}
                      </span>
                    </label>
                  ))}
                </div>
              )}
              {errors.modalityIds?.message && (
                <p className="mt-2 text-xs text-[#ff8a8a]">{errors.modalityIds.message}</p>
              )}
            </fieldset>
            <fieldset>
              <legend className="text-sm font-bold">Tipos de atendimento</legend>
              <div className="mt-3 grid gap-2">
                {[
                  ['IN_PERSON', 'Presencial'],
                  ['HOME_VISIT', 'Atendimento em domicílio'],
                  ['ONLINE', 'Online'],
                ].map(([value, label]) => (
                  <label className="flex items-center gap-3 rounded-xl border border-[#333] bg-[#171717] p-3 text-sm" key={value}>
                    <input disabled={isSubmitting} type="checkbox" value={value} {...register('serviceModes')} />
                    {label}
                  </label>
                ))}
              </div>
            </fieldset>
          </div>
        </section>

        <section className="rounded-[26px] border border-[#292929] bg-[#111] p-6 md:p-7">
          <SectionHeading
            description="Cadastre cidades, bairros ou observações específicas de cobertura."
            eyebrow="COBERTURA"
            title="Regiões de atendimento"
          />
          <div className="mt-6 grid gap-4">
            {serviceAreas.fields.map((field, index) => (
              <div className="rounded-2xl border border-[#333] bg-[#171717] p-5" key={field.id}>
                <div className="flex items-center justify-between gap-3">
                  <p className="font-bold">Região {index + 1}</p>
                  <button className="text-sm font-bold text-[#ff8a8a]" disabled={isSubmitting} onClick={() => serviceAreas.remove(index)} type="button">Remover</button>
                </div>
                <div className="mt-4 grid gap-4 sm:grid-cols-[1fr_100px]">
                  <div>
                    <label className="text-xs font-bold text-[#aaa]" htmlFor={`area-city-${index}`}>Cidade</label>
                    <input className={inputClassName} disabled={isSubmitting} id={`area-city-${index}`} {...register(`serviceAreas.${index}.city`)} />
                    <ErrorText error={errors.serviceAreas?.[index]?.city} />
                  </div>
                  <div>
                    <label className="text-xs font-bold text-[#aaa]" htmlFor={`area-state-${index}`}>UF</label>
                    <input className={`${inputClassName} uppercase`} disabled={isSubmitting} id={`area-state-${index}`} maxLength={2} {...register(`serviceAreas.${index}.stateCode`)} />
                    <ErrorText error={errors.serviceAreas?.[index]?.stateCode} />
                  </div>
                  <div>
                    <label className="text-xs font-bold text-[#aaa]" htmlFor={`area-neighborhood-${index}`}>Bairro</label>
                    <input className={inputClassName} disabled={isSubmitting} id={`area-neighborhood-${index}`} {...register(`serviceAreas.${index}.neighborhood`)} />
                    <ErrorText error={errors.serviceAreas?.[index]?.neighborhood} />
                  </div>
                  <div>
                    <label className="text-xs font-bold text-[#aaa]" htmlFor={`area-description-${index}`}>Observação</label>
                    <input className={inputClassName} disabled={isSubmitting} id={`area-description-${index}`} {...register(`serviceAreas.${index}.description`)} />
                    <ErrorText error={errors.serviceAreas?.[index]?.description} />
                  </div>
                </div>
              </div>
            ))}
            <button
              className="w-fit rounded-full border border-[#7657ff]/50 px-5 py-2.5 text-sm font-bold text-[#b5a9ff] transition hover:bg-[#7657ff]/10"
              disabled={isSubmitting}
              onClick={() => serviceAreas.append({ city: '', stateCode: '', neighborhood: '', description: '' })}
              type="button">
              Adicionar região
            </button>
          </div>
        </section>

        <section className="rounded-[26px] border border-[#292929] bg-[#111] p-6 md:p-7">
          <SectionHeading
            description="Opcional. Enquanto o upload estiver indisponível, use somente uma chave privada de documento já preparada."
            eyebrow="REGISTRO"
            title="CREF"
          />
          <div className="mt-6 grid gap-5 md:grid-cols-2">
            <div>
              <label className="text-sm font-bold" htmlFor="cref-code">Número do CREF</label>
              <input className={inputClassName} disabled={isSubmitting} id="cref-code" {...register('crefRegistrationCode')} />
              <ErrorText error={errors.crefRegistrationCode} />
            </div>
            <div>
              <label className="text-sm font-bold" htmlFor="cref-document-key">Chave privada do documento</label>
              <input autoComplete="off" className={inputClassName} disabled={isSubmitting} id="cref-document-key" placeholder="private/crefs/.../document.webp" type="password" {...register('crefDocumentImageKey')} />
              <ErrorText error={errors.crefDocumentImageKey} />
            </div>
          </div>
        </section>

        <section className="rounded-[26px] border border-[#c7ff3d]/25 bg-[#111] p-6 md:p-7">
          <SectionHeading
            description="Esta justificativa será registrada na auditoria administrativa."
            eyebrow="AUDITORIA"
            title="Justificativa da operação"
          />
          <div className="mt-6">
            <label className="text-sm font-bold" htmlFor="admin-reason">Motivo</label>
            <textarea className={textareaClassName} disabled={isSubmitting} id="admin-reason" placeholder="Explique por que este cadastro ou ajuste está sendo realizado" {...register('reason')} />
            <ErrorText error={errors.reason} />
          </div>
          <div className="mt-6 flex justify-end">
            <button
              className="rounded-full bg-[#c7ff3d] px-6 py-3.5 text-sm font-extrabold text-[#080808] transition hover:bg-[#d6ff70] disabled:cursor-wait disabled:opacity-50"
              disabled={isSubmitting || modalitiesQuery.isError}
              type="submit">
              {isSubmitting ? 'Salvando…' : isCreate ? 'Revisar e cadastrar' : 'Revisar e salvar'}
            </button>
          </div>
        </section>
      </form>

      {pendingValues && (
        <div aria-labelledby="personal-confirmation-title" aria-modal="true" className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-5 backdrop-blur-sm" role="dialog">
          <div className="w-full max-w-lg rounded-[28px] border border-[#333] bg-[#111] p-7 shadow-2xl">
            <p className="text-xs font-bold tracking-[.14em] text-[#c7ff3d]">CONFIRMAR OPERAÇÃO</p>
            <h2 className="mt-3 font-['Manrope'] text-2xl font-extrabold" id="personal-confirmation-title">
              {isCreate ? `Cadastrar ${pendingValues.fullName}?` : `Salvar alterações de ${pendingValues.fullName}?`}
            </h2>
            <p className="mt-3 text-sm leading-6 text-[#aaa]">
              {isCreate
                ? 'Uma nova conta e um perfil em rascunho serão criados. A senha temporária deverá ser compartilhada por um canal seguro.'
                : 'Os dados da revisão editável serão substituídos e a justificativa ficará registrada na auditoria.'}
            </p>
            <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <button className="rounded-xl border border-[#444] px-5 py-3 text-sm font-bold" disabled={isSubmitting} onClick={() => setPendingValues(undefined)} type="button">Voltar e revisar</button>
              <button className="rounded-xl bg-[#c7ff3d] px-5 py-3 text-sm font-extrabold text-[#080808] disabled:opacity-50" disabled={isSubmitting} onClick={() => void confirmSubmit()} type="button">
                {isSubmitting ? 'Salvando…' : isCreate ? 'Confirmar cadastro' : 'Confirmar alterações'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
