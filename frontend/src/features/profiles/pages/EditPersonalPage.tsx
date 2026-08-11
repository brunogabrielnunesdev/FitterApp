import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'

import { PersonalManagementForm } from '../components/PersonalManagementForm'
import { ProfileDetailState } from '../components/ProfileDetailState'
import { getAdminProfile } from '../services/adminProfileService'
import { updatePersonal } from '../services/personalManagementService'
import { profileQueryKeys } from '../services/profileQueryKeys'
import type { PersonalFormValues, UpdatePersonalRequest } from '../types/personalManagement'
import { getModerationErrorMessage } from '../utils/getModerationErrorMessage'
import { getPersonalManagementErrorMessage } from '../utils/getPersonalManagementErrorMessage'
import { personalFormValuesFromDetail, toUpdatePersonalRequest } from '../utils/personalFormMapper'

export default function EditPersonalPage() {
  const { profileId = '' } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const profileQuery = useQuery({
    queryKey: profileQueryKeys.detail(profileId),
    queryFn: () => getAdminProfile(profileId),
    enabled: Boolean(profileId),
    retry: false,
  })
  const updateMutation = useMutation({
    mutationFn: (request: UpdatePersonalRequest) => updatePersonal(profileId, request),
  })

  async function submit(values: PersonalFormValues) {
    await updateMutation.mutateAsync(toUpdatePersonalRequest(values))
    await queryClient.invalidateQueries({ queryKey: profileQueryKeys.all })
    navigate(`/admin/personals/${profileId}`, {
      replace: true,
      state: { managementMessage: `As alterações de ${values.fullName.trim()} foram salvas.` },
    })
  }

  const profile = profileQuery.data
  const isEditable =
    profile?.revision.status === 'DRAFT' || profile?.revision.status === 'REJECTED'

  return (
    <div className="mx-auto max-w-5xl px-6 py-10 lg:px-10 lg:py-14">
      <div className="flex flex-col gap-5 border-b border-[#292929] pb-8 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-bold tracking-[.18em] text-[#c7ff3d]">EDIÇÃO ADMINISTRATIVA</p>
          <h1 className="mt-3 font-['Manrope'] text-4xl font-extrabold tracking-[-.045em] md:text-5xl">
            {profile?.revision.fullName ?? 'Editar personal'}
          </h1>
          <p className="mt-3 max-w-2xl text-[#aaaaaa]">
            Ajuste somente os campos liberados pelo contrato da revisão editável.
          </p>
        </div>
        <Link className="w-fit rounded-full border border-[#333] px-5 py-2.5 text-sm font-bold transition hover:border-[#c7ff3d] hover:text-[#c7ff3d]" to={`/admin/personals/${profileId}`}>
          Cancelar
        </Link>
      </div>

      <div className="mt-8">
        {profileQuery.isPending && <ProfileDetailState status="loading" />}
        {profileQuery.isError && (
          <ProfileDetailState
            isRetrying={profileQuery.isFetching}
            message={getModerationErrorMessage(profileQuery.error, 'carregar')}
            onRetry={() => void profileQuery.refetch()}
            status="error"
          />
        )}
        {profile && !isEditable && (
          <div className="rounded-[26px] border border-[#ff9f43]/40 bg-[#ff9f43]/8 p-8">
            <h2 className="font-['Manrope'] text-xl font-extrabold">Esta revisão não pode ser editada</h2>
            <p className="mt-2 max-w-xl text-sm leading-6 text-[#c8b8a5]">
              A edição administrativa está disponível somente para revisões em rascunho ou reprovadas. Revisões enviadas, aprovadas ou publicadas precisam seguir o fluxo de nova revisão.
            </p>
            <Link className="mt-6 inline-flex rounded-full bg-[#f6f4ee] px-5 py-2.5 text-sm font-bold text-[#080808]" to={`/admin/personals/${profileId}`}>
              Voltar ao detalhe
            </Link>
          </div>
        )}
        {updateMutation.isError && (
          <div className="mb-6 rounded-2xl border border-[#ff6b6b]/40 bg-[#ff6b6b]/10 px-5 py-4 text-sm text-[#ffb0b0]" role="alert">
            <p className="font-bold">As alterações não foram salvas</p>
            <p className="mt-1 text-[#f6f4ee]">{getPersonalManagementErrorMessage(updateMutation.error)}</p>
          </div>
        )}
        {profile && isEditable && (
          <PersonalManagementForm
            defaultValues={personalFormValuesFromDetail(profile)}
            isSubmitting={updateMutation.isPending}
            mode="edit"
            onSubmit={submit}
          />
        )}
      </div>
    </div>
  )
}
