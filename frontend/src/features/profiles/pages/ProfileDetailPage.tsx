import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'

import { Logo } from '../../../common/components/Logo'
import { ModerationFeedbackBanner } from '../components/ModerationFeedbackBanner'
import { ProfileDetailState } from '../components/ProfileDetailState'
import { REJECTION_REASON_MAX_LENGTH } from '../components/PendingProfileCard'
import { getAdminProfile } from '../services/adminProfileService'
import { approveProfile, rejectProfile } from '../services/profileModerationService'
import type { ModerationAction, ModerationFeedback } from '../types/moderation'
import type {
  AccountStatus,
  AdminProfileDetail,
  CrefStatus,
  PriceUnit,
  ProfileStatus,
  RevisionStatus,
  ServiceMode,
} from '../types/profileDetail'
import { getModerationErrorMessage } from '../utils/getModerationErrorMessage'

const profileStatusLabels: Record<ProfileStatus, string> = {
  DRAFT: 'Rascunho',
  PENDING_REVIEW: 'Aguardando análise',
  APPROVED: 'Aprovado',
  PUBLISHED: 'Publicado',
  REJECTED: 'Reprovado',
  SUSPENDED: 'Suspenso',
}

const revisionStatusLabels: Record<RevisionStatus, string> = {
  DRAFT: 'Rascunho',
  PENDING_REVIEW: 'Aguardando análise',
  APPROVED: 'Aprovada',
  REJECTED: 'Reprovada',
}

const accountStatusLabels: Record<AccountStatus, string> = {
  PENDING_VERIFICATION: 'E-mail pendente',
  ACTIVE: 'Ativa',
  BLOCKED: 'Bloqueada',
}

const crefStatusLabels: Record<CrefStatus, string> = {
  PENDING_REVIEW: 'Aguardando análise',
  VERIFIED: 'Verificado',
  REJECTED: 'Reprovado',
}

const serviceModeLabels: Record<ServiceMode, string> = {
  IN_PERSON: 'Presencial',
  HOME_VISIT: 'Atendimento em domicílio',
  ONLINE: 'Online',
}

const priceUnitLabels: Record<PriceUnit, string> = {
  PER_SESSION: 'por sessão',
  MONTHLY: 'por mês',
  CONSULTATION: 'por consulta',
}

function formatDate(value: string | null) {
  if (!value) return 'Não informado'
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function formatPrice(cents: number | null, unit: PriceUnit | null) {
  if (cents === null) return 'Não informado'
  const value = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(cents / 100)
  return unit ? `${value} ${priceUnitLabels[unit]}` : value
}

function DetailCard({
  title,
  eyebrow,
  children,
  className = '',
}: {
  title: string
  eyebrow: string
  children: React.ReactNode
  className?: string
}) {
  return (
    <section className={`rounded-[26px] border border-[#292929] bg-[#111] p-6 md:p-7 ${className}`}>
      <p className="text-[11px] font-bold tracking-[.15em] text-[#777]">{eyebrow}</p>
      <h2 className="mt-2 font-['Manrope'] text-xl font-extrabold">{title}</h2>
      <div className="mt-6">{children}</div>
    </section>
  )
}

function DataItem({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <dt className="text-xs font-bold tracking-[.08em] text-[#777] uppercase">{label}</dt>
      <dd className="mt-1.5 break-words text-sm leading-6 text-[#e3e0d9]">{children}</dd>
    </div>
  )
}

function ProfileContent({ profile }: { profile: AdminProfileDetail }) {
  const { revision, account } = profile

  return (
    <div className="grid gap-5 lg:grid-cols-2">
      <DetailCard eyebrow="CONTA" title="Dados pessoais permitidos">
        <dl className="grid gap-5 sm:grid-cols-2">
          <DataItem label="Nome da conta">{account.fullName}</DataItem>
          <DataItem label="Status da conta">{accountStatusLabels[account.status]}</DataItem>
          <DataItem label="E-mail">
            <a className="hover:text-[#c7ff3d]" href={`mailto:${account.email}`}>
              {account.email}
            </a>
          </DataItem>
          <DataItem label="Telefone">{account.phoneNumber}</DataItem>
        </dl>
      </DetailCard>

      <DetailCard eyebrow="REVISÃO" title="Situação da análise">
        <dl className="grid gap-5 sm:grid-cols-2">
          <DataItem label="Status do perfil">{profileStatusLabels[profile.status]}</DataItem>
          <DataItem label="Status da revisão">{revisionStatusLabels[revision.status]}</DataItem>
          <DataItem label="Versão">#{revision.versionNumber}</DataItem>
          <DataItem label="Enviado em">{formatDate(revision.submittedAt)}</DataItem>
          <DataItem label="Publicado">{profile.published ? 'Sim' : 'Não'}</DataItem>
          <DataItem label="Atualizado em">{formatDate(revision.updatedAt)}</DataItem>
        </dl>
        {revision.rejectionReason && (
          <div className="mt-5 rounded-xl border border-[#ff6b6b]/30 bg-[#ff6b6b]/8 p-4">
            <p className="text-xs font-bold text-[#ff9b9b]">MOTIVO DA REPROVAÇÃO</p>
            <p className="mt-2 text-sm leading-6 text-[#d8caca]">{revision.rejectionReason}</p>
          </div>
        )}
      </DetailCard>

      <DetailCard className="lg:col-span-2" eyebrow="APRESENTAÇÃO" title={revision.fullName}>
        <div className="grid gap-7 lg:grid-cols-[1.4fr_.6fr]">
          <div>
            <p className="text-xs font-bold tracking-[.08em] text-[#777] uppercase">Biografia</p>
            <p className="mt-2 whitespace-pre-line text-sm leading-7 text-[#d6d2ca]">
              {revision.biography || 'Não informada'}
            </p>
          </div>
          <dl className="grid content-start gap-5 sm:grid-cols-2 lg:grid-cols-1">
            <DataItem label="Experiência">
              {revision.experienceStartedYear
                ? `Desde ${revision.experienceStartedYear}`
                : 'Não informada'}
            </DataItem>
            <DataItem label="Preço inicial">
              {formatPrice(revision.startingPriceCents, revision.priceUnit)}
            </DataItem>
            <DataItem label="WhatsApp">
              <a
                className="font-bold text-[#c7ff3d] hover:underline"
                href={`https://wa.me/${revision.whatsapp.replace(/\D/g, '')}`}
                rel="noreferrer"
                target="_blank">
                {revision.whatsapp}
              </a>
            </DataItem>
          </dl>
        </div>
        <div className="mt-7 grid gap-6 border-t border-[#292929] pt-6 md:grid-cols-2">
          <DataItem label="Certificações">{revision.certifications || 'Não informadas'}</DataItem>
          <DataItem label="Academias e locais">{revision.gymsDescription || 'Não informados'}</DataItem>
        </div>
      </DetailCard>

      <DetailCard eyebrow="REGISTRO PROFISSIONAL" title="CREF">
        {revision.cref ? (
          <dl className="grid gap-5 sm:grid-cols-2">
            <DataItem label="Número">{revision.cref.registrationCode}</DataItem>
            <DataItem label="Status">{crefStatusLabels[revision.cref.status]}</DataItem>
            <DataItem label="Documento">
              {revision.cref.documentImageKey ? 'Documento anexado' : 'Não anexado'}
            </DataItem>
            <DataItem label="Verificado em">{formatDate(revision.cref.verifiedAt)}</DataItem>
            {revision.cref.rejectionReason && (
              <div className="sm:col-span-2">
                <DataItem label="Motivo da reprovação">{revision.cref.rejectionReason}</DataItem>
              </div>
            )}
          </dl>
        ) : (
          <div className="rounded-xl border border-[#333] bg-[#171717] p-5">
            <p className="font-bold text-[#d6d2ca]">CREF não informado</p>
            <p className="mt-1 text-sm text-[#888]">O registro profissional é opcional neste fluxo.</p>
          </div>
        )}
      </DetailCard>

      <DetailCard eyebrow="ESPECIALIDADES" title="Modalidades">
        {revision.modalities.length > 0 ? (
          <ul className="flex flex-wrap gap-2">
            {revision.modalities.map((modality) => (
              <li
                className="rounded-full border border-[#7657ff]/40 bg-[#7657ff]/10 px-3.5 py-2 text-sm font-bold text-[#b5a9ff]"
                key={modality.id}>
                {modality.name}
                {!modality.active && <span className="ml-1 text-[#777]">(inativa)</span>}
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-[#888]">Nenhuma modalidade informada.</p>
        )}
      </DetailCard>

      <DetailCard eyebrow="FORMATO" title="Tipos de atendimento">
        {revision.serviceModes.length > 0 ? (
          <ul className="grid gap-3 sm:grid-cols-2">
            {revision.serviceModes.map((mode) => (
              <li className="rounded-xl border border-[#333] bg-[#171717] px-4 py-3 text-sm" key={mode}>
                {serviceModeLabels[mode]}
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-[#888]">Nenhum tipo de atendimento informado.</p>
        )}
      </DetailCard>

      <DetailCard eyebrow="COBERTURA" title="Regiões de atendimento">
        {revision.serviceAreas.length > 0 ? (
          <ul className="grid gap-3">
            {revision.serviceAreas.map((area) => (
              <li className="rounded-xl border border-[#333] bg-[#171717] p-4" key={area.id}>
                <p className="text-sm font-bold">
                  {[area.neighborhood, area.city, area.stateCode].filter(Boolean).join(' · ')}
                </p>
                {area.description && <p className="mt-1.5 text-sm text-[#888]">{area.description}</p>}
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-[#888]">Nenhuma região informada.</p>
        )}
      </DetailCard>
    </div>
  )
}

export function ProfileDetailPage() {
  const { profileId = '' } = useParams()
  const queryClient = useQueryClient()
  const [rejectionReason, setRejectionReason] = useState('')
  const [processingAction, setProcessingAction] = useState<ModerationAction>()
  const [feedback, setFeedback] = useState<ModerationFeedback>()
  const profileQueryKey = ['admin-personal-profile', profileId] as const
  const profileQuery = useQuery({
    queryKey: profileQueryKey,
    queryFn: () => getAdminProfile(profileId),
    enabled: Boolean(profileId),
    retry: false,
  })

  async function runAction(action: ModerationAction) {
    const profile = profileQuery.data
    if (!profile) return

    const normalizedReason = rejectionReason.trim()
    if (action === 'reject' && !normalizedReason) return
    setProcessingAction(action)
    setFeedback(undefined)

    try {
      if (action === 'approve') await approveProfile(profile.profileId)
      else await rejectProfile(profile.profileId, normalizedReason)

      if (action === 'reject') setRejectionReason('')
      setFeedback({
        profileId: profile.profileId,
        profileName: profile.revision.fullName,
        action,
        status: 'success',
        message:
          action === 'approve'
            ? `${profile.revision.fullName} foi aprovado com sucesso.`
            : `${profile.revision.fullName} foi reprovado e poderá corrigir o perfil.`,
      })
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['pending-personal-profiles'] }),
        queryClient.invalidateQueries({ queryKey: profileQueryKey }),
      ])
    } catch (error) {
      setFeedback({
        profileId: profile.profileId,
        profileName: profile.revision.fullName,
        action,
        status: 'error',
        message: getModerationErrorMessage(error, action === 'approve' ? 'aprovar' : 'reprovar'),
      })
    } finally {
      setProcessingAction(undefined)
    }
  }

  const profile = profileQuery.data
  const canModerate =
    profile?.status === 'PENDING_REVIEW' && profile.revision.status === 'PENDING_REVIEW'
  const normalizedReason = rejectionReason.trim()
  const validReason =
    normalizedReason.length > 0 && normalizedReason.length <= REJECTION_REASON_MAX_LENGTH

  return (
    <main className="min-h-screen bg-[#080808] text-[#f6f4ee]">
      <header className="border-b border-[#292929] bg-[#080808]/95 px-6 backdrop-blur lg:px-10">
        <div className="mx-auto flex h-20 max-w-6xl items-center justify-between">
          <Link aria-label="Voltar ao dashboard" to="/admin">
            <Logo compact />
          </Link>
          <Link
            className="rounded-full border border-[#292929] px-5 py-2.5 text-sm font-bold transition hover:border-[#c7ff3d] hover:text-[#c7ff3d]"
            to="/admin/personals/pending">
            Voltar à fila
          </Link>
        </div>
      </header>

      <div className="mx-auto max-w-6xl px-6 py-10 lg:py-14">
        <div className="border-b border-[#292929] pb-8">
          <p className="text-xs font-bold tracking-[.18em] text-[#c7ff3d]">ANÁLISE DE PERFIL</p>
          <h1 className="mt-3 font-['Manrope'] text-4xl font-extrabold tracking-[-.045em] md:text-5xl">
            {profile?.revision.fullName ?? 'Detalhes do personal'}
          </h1>
          <p className="mt-3 max-w-2xl text-[#aaaaaa]">
            Confira os dados profissionais, o registro e a cobertura antes de decidir.
          </p>
        </div>

        <div className="mt-8">
          {feedback && (
            <ModerationFeedbackBanner feedback={feedback} onDismiss={() => setFeedback(undefined)} />
          )}
          {profileQuery.isPending && <ProfileDetailState status="loading" />}
          {profileQuery.isError && (
            <ProfileDetailState
              isRetrying={profileQuery.isFetching}
              message={getModerationErrorMessage(profileQuery.error, 'carregar')}
              onRetry={() => void profileQuery.refetch()}
              status="error"
            />
          )}
          {profile && <ProfileContent profile={profile} />}
        </div>

        {profile && canModerate && (
          <section className="sticky bottom-4 z-10 mt-6 rounded-[26px] border border-[#7657ff]/45 bg-[#111]/95 p-5 shadow-2xl backdrop-blur md:p-6">
            <div className="flex flex-col gap-5 lg:flex-row lg:items-end">
              <div className="min-w-0 flex-1">
                <label className="text-sm font-bold" htmlFor="detail-rejection-reason">
                  Motivo da reprovação
                </label>
                <p className="mt-1 text-xs text-[#777]">Obrigatório somente para reprovar.</p>
                <textarea
                  aria-describedby="detail-rejection-counter"
                  className="mt-3 min-h-20 w-full resize-y rounded-xl border border-[#333] bg-[#171717] px-4 py-3 text-sm outline-none transition placeholder:text-[#666] focus:border-[#7657ff] disabled:cursor-wait disabled:opacity-55"
                  disabled={processingAction !== undefined}
                  id="detail-rejection-reason"
                  maxLength={REJECTION_REASON_MAX_LENGTH}
                  onChange={(event) => setRejectionReason(event.target.value)}
                  placeholder="Explique o que precisa ser corrigido"
                  value={rejectionReason}
                />
                <p className="mt-1 text-right text-xs text-[#777]" id="detail-rejection-counter">
                  {rejectionReason.length}/{REJECTION_REASON_MAX_LENGTH}
                </p>
              </div>
              <div className="flex flex-col-reverse gap-2 sm:flex-row lg:pb-5">
                <button
                  className="rounded-xl border border-[#ff6b6b]/60 px-5 py-3 text-sm font-bold text-[#ff8a8a] transition hover:bg-[#ff6b6b]/10 disabled:cursor-not-allowed disabled:opacity-35"
                  disabled={processingAction !== undefined || !validReason}
                  onClick={() => void runAction('reject')}
                  type="button">
                  {processingAction === 'reject' ? 'Reprovando…' : 'Reprovar perfil'}
                </button>
                <button
                  className="rounded-xl bg-[#c7ff3d] px-5 py-3 text-sm font-extrabold text-[#080808] transition hover:bg-[#d6ff70] disabled:cursor-wait disabled:opacity-55"
                  disabled={processingAction !== undefined}
                  onClick={() => void runAction('approve')}
                  type="button">
                  {processingAction === 'approve' ? 'Aprovando…' : 'Aprovar perfil'}
                </button>
              </div>
            </div>
          </section>
        )}
      </div>
    </main>
  )
}
