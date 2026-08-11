import type { ModerationAction, PendingProfile } from '../types/moderation'
import { Link } from 'react-router-dom'

export const REJECTION_REASON_MAX_LENGTH = 500

type PendingProfileCardProps = {
  profile: PendingProfile
  rejectionReason: string
  processingAction?: ModerationAction
  onReasonChange: (reason: string) => void
  onApprove: () => void
  onReject: () => void
}

export function PendingProfileCard({
  profile,
  rejectionReason,
  processingAction,
  onReasonChange,
  onApprove,
  onReject,
}: PendingProfileCardProps) {
  const isProcessing = processingAction !== undefined
  const normalizedReason = rejectionReason.trim()
  const isReasonValid = normalizedReason.length > 0 && normalizedReason.length <= REJECTION_REASON_MAX_LENGTH
  const reasonId = `rejection-reason-${profile.profileId}`

  return (
    <article className="rounded-[26px] border border-[#292929] bg-[#111] p-6 transition hover:border-[#3a3a3a] md:p-7">
      <div className="flex flex-col justify-between gap-5 md:flex-row md:items-start">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <span className="rounded-full bg-[#7657ff]/15 px-3 py-1 text-[11px] font-bold tracking-[.12em] text-[#a999ff]">
              AGUARDANDO ANÁLISE
            </span>
            {profile.published && (
              <span className="rounded-full border border-[#292929] px-3 py-1 text-[11px] font-bold tracking-[.1em] text-[#aaaaaa]">
                POSSUI VERSÃO PUBLICADA
              </span>
            )}
          </div>
          <h2 className="mt-3 font-['Manrope'] text-2xl font-extrabold tracking-[-.025em]">
            {profile.fullName}
          </h2>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link
            className="rounded-full border border-[#444] px-5 py-3 text-sm font-extrabold transition hover:border-[#7657ff] hover:text-[#a999ff]"
            state={{ from: '/admin/personals/pending' }}
            to={`/admin/personals/${profile.profileId}`}>
            Analisar detalhes
          </Link>
          <button
            className="rounded-full bg-[#c7ff3d] px-5 py-3 text-sm font-extrabold text-[#080808] transition hover:bg-[#d6ff70] disabled:cursor-wait disabled:opacity-55"
            disabled={isProcessing}
            onClick={onApprove}
            type="button">
            {processingAction === 'approve' ? 'Aprovando…' : 'Aprovar perfil'}
          </button>
        </div>
      </div>

      <div className="mt-6 border-t border-[#292929] pt-5">
        <label className="text-sm font-bold" htmlFor={reasonId}>
          Motivo da reprovação
        </label>
        <p className="mt-1 text-xs text-[#777]">Obrigatório e visível para o personal.</p>
        <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-start">
          <div className="min-w-0 flex-1">
            <textarea
              aria-describedby={`${reasonId}-counter`}
              className="min-h-24 w-full resize-y rounded-xl border border-[#333] bg-[#171717] px-4 py-3 text-sm text-[#f6f4ee] outline-none transition placeholder:text-[#666] focus:border-[#7657ff] disabled:cursor-wait disabled:opacity-55"
              disabled={isProcessing}
              id={reasonId}
              maxLength={REJECTION_REASON_MAX_LENGTH}
              onChange={(event) => onReasonChange(event.target.value)}
              placeholder="Explique claramente o que precisa ser corrigido"
              value={rejectionReason}
            />
            <p className="mt-1 text-right text-xs text-[#777]" id={`${reasonId}-counter`}>
              {rejectionReason.length}/{REJECTION_REASON_MAX_LENGTH}
            </p>
          </div>
          <button
            className="rounded-xl border border-[#ff6b6b]/60 px-5 py-3 text-sm font-bold text-[#ff8a8a] transition hover:border-[#ff8a8a] hover:bg-[#ff6b6b]/10 disabled:cursor-not-allowed disabled:opacity-35"
            disabled={isProcessing || !isReasonValid}
            onClick={onReject}
            type="button">
            {processingAction === 'reject' ? 'Reprovando…' : 'Reprovar perfil'}
          </button>
        </div>
      </div>
    </article>
  )
}
