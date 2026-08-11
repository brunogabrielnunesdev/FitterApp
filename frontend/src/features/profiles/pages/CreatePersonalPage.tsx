import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'

import { PersonalManagementForm } from '../components/PersonalManagementForm'
import { createPersonal } from '../services/personalManagementService'
import { profileQueryKeys } from '../services/profileQueryKeys'
import type { PersonalFormValues } from '../types/personalManagement'
import { getPersonalManagementErrorMessage } from '../utils/getPersonalManagementErrorMessage'
import { emptyPersonalFormValues, toCreatePersonalRequest } from '../utils/personalFormMapper'

export default function CreatePersonalPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const createMutation = useMutation({ mutationFn: createPersonal })

  async function submit(values: PersonalFormValues) {
    const result = await createMutation.mutateAsync(toCreatePersonalRequest(values))
    await queryClient.invalidateQueries({ queryKey: profileQueryKeys.all })
    navigate(`/admin/personals/${result.profileId}`, {
      replace: true,
      state: { managementMessage: `${values.fullName.trim()} foi cadastrado em rascunho.` },
    })
  }

  return (
    <div className="mx-auto max-w-5xl px-6 py-10 lg:px-10 lg:py-14">
      <div className="flex flex-col gap-5 border-b border-[#292929] pb-8 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-bold tracking-[.18em] text-[#c7ff3d]">CADASTRO ADMINISTRATIVO</p>
          <h1 className="mt-3 font-['Manrope'] text-4xl font-extrabold tracking-[-.045em] md:text-5xl">
            Novo personal
          </h1>
          <p className="mt-3 max-w-2xl text-[#aaaaaa]">
            Crie a conta e prepare o perfil profissional para revisão posterior.
          </p>
        </div>
        <Link className="w-fit rounded-full border border-[#333] px-5 py-2.5 text-sm font-bold transition hover:border-[#c7ff3d] hover:text-[#c7ff3d]" to="/admin/personals">
          Cancelar
        </Link>
      </div>

      {createMutation.isError && (
        <div className="mt-8 rounded-2xl border border-[#ff6b6b]/40 bg-[#ff6b6b]/10 px-5 py-4 text-sm text-[#ffb0b0]" role="alert">
          <p className="font-bold">O cadastro não foi concluído</p>
          <p className="mt-1 text-[#f6f4ee]">{getPersonalManagementErrorMessage(createMutation.error)}</p>
        </div>
      )}

      <div className="mt-8">
        <PersonalManagementForm
          defaultValues={emptyPersonalFormValues()}
          isSubmitting={createMutation.isPending}
          mode="create"
          onSubmit={submit}
        />
      </div>
    </div>
  )
}
