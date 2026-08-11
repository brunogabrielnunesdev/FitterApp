import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'

import { ModerationFeedbackBanner } from '../components/ModerationFeedbackBanner'
import { PendingProfileCard } from '../components/PendingProfileCard'
import { QueueState } from '../components/QueueState'
import {
  approveProfile,
  listPendingProfiles,
  rejectProfile,
} from '../services/profileModerationService'
import { profileQueryKeys } from '../services/profileQueryKeys'
import type {
  ModerationAction,
  ModerationFeedback,
  PendingProfile,
} from '../types/moderation'
import { getModerationErrorMessage } from '../utils/getModerationErrorMessage'

export function PendingProfilesPage() {
  const queryClient = useQueryClient()
  const [reasonByProfile, setReasonByProfile] = useState<Record<string, string>>({})
  const [processingByProfile, setProcessingByProfile] = useState<Record<string, ModerationAction>>({})
  const [feedbacks, setFeedbacks] = useState<ModerationFeedback[]>([])
  const pendingQuery = useQuery({
    queryKey: profileQueryKeys.pending,
    queryFn: listPendingProfiles,
    retry: false,
  })

  function removeFromQueue(profileId: string) {
    queryClient.setQueryData<PendingProfile[]>(profileQueryKeys.pending, (profiles) =>
      profiles?.filter((profile) => profile.profileId !== profileId),
    )
    void queryClient.invalidateQueries({ queryKey: profileQueryKeys.all })
  }

  async function runAction(profile: PendingProfile, action: ModerationAction) {
    setProcessingByProfile((current) => ({ ...current, [profile.profileId]: action }))
    setFeedbacks((current) => current.filter((feedback) => feedback.profileId !== profile.profileId))

    try {
      if (action === 'approve') {
        await approveProfile(profile.profileId)
      } else {
        const normalizedReason = reasonByProfile[profile.profileId]?.trim() ?? ''
        if (!normalizedReason) return
        await rejectProfile(profile.profileId, normalizedReason)
        setReasonByProfile((current) => {
          const next = { ...current }
          delete next[profile.profileId]
          return next
        })
      }

      removeFromQueue(profile.profileId)
      setFeedbacks((current) => [
        {
          profileId: profile.profileId,
          profileName: profile.fullName,
          action,
          status: 'success',
          message:
            action === 'approve'
              ? `${profile.fullName} foi aprovado e removido da fila.`
              : `${profile.fullName} foi reprovado e poderá corrigir o perfil.`,
        },
        ...current.filter((item) => item.profileId !== profile.profileId),
      ])
    } catch (error) {
      setFeedbacks((current) => [
        {
          profileId: profile.profileId,
          profileName: profile.fullName,
          action,
          status: 'error',
          message: getModerationErrorMessage(error, action === 'approve' ? 'aprovar' : 'reprovar'),
        },
        ...current.filter((item) => item.profileId !== profile.profileId),
      ])
    } finally {
      setProcessingByProfile((current) => {
        const next = { ...current }
        delete next[profile.profileId]
        return next
      })
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-6 py-10 lg:py-14">
        <div className="flex flex-col gap-4 border-b border-[#292929] pb-8 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-xs font-bold tracking-[.18em] text-[#c7ff3d]">MODERAÇÃO</p>
            <h1 className="mt-3 font-['Manrope'] text-4xl font-extrabold tracking-[-.045em] md:text-5xl">
              Perfis aguardando análise
            </h1>
            <p className="mt-3 max-w-2xl text-[#aaaaaa]">
              Aprovar libera o acesso de personal. Reprovar devolve o perfil para correção.
            </p>
          </div>
          {pendingQuery.isSuccess && (
            <span className="whitespace-nowrap text-sm font-bold text-[#aaaaaa]">
              {pendingQuery.data.length}{' '}
              {pendingQuery.data.length === 1 ? 'perfil pendente' : 'perfis pendentes'}
            </span>
          )}
        </div>

        <section aria-label="Fila de perfis" className="mt-8">
          {feedbacks.map((feedback) => (
            <ModerationFeedbackBanner
              feedback={feedback}
              key={feedback.profileId}
              onDismiss={() =>
                setFeedbacks((current) =>
                  current.filter((item) => item.profileId !== feedback.profileId),
                )
              }
            />
          ))}
          {pendingQuery.isPending && <QueueState status="loading" />}
          {pendingQuery.isError && (
            <QueueState
              isRetrying={pendingQuery.isFetching}
              message={getModerationErrorMessage(pendingQuery.error, 'carregar')}
              onRetry={() => void pendingQuery.refetch()}
              status="error"
            />
          )}
          {pendingQuery.isSuccess && pendingQuery.data.length === 0 && <QueueState status="empty" />}
          {pendingQuery.isSuccess && pendingQuery.data.length > 0 && (
            <div className="grid gap-4">
              {pendingQuery.data.map((profile) => (
                <PendingProfileCard
                  key={profile.profileId}
                  onApprove={() => void runAction(profile, 'approve')}
                  onReasonChange={(reason) =>
                    setReasonByProfile((current) => ({ ...current, [profile.profileId]: reason }))
                  }
                  onReject={() => void runAction(profile, 'reject')}
                  processingAction={processingByProfile[profile.profileId]}
                  profile={profile}
                  rejectionReason={reasonByProfile[profile.profileId] ?? ''}
                />
              ))}
            </div>
          )}
        </section>
    </div>
  )
}
