import { useEffect, useState } from 'react'

import type { ProfileStatus } from '../types/profileDetail'
import type { ProfileLifecycleAction } from '../types/profileManagement'

const REASON_MAX_LENGTH = 1500

type ProfileLifecyclePanelProps = {
  profileName: string
  status: ProfileStatus
  isProcessing: boolean
  onConfirm: (action: ProfileLifecycleAction, reason: string) => void
}

export function ProfileLifecyclePanel({
  profileName,
  status,
  isProcessing,
  onConfirm,
}: ProfileLifecyclePanelProps) {
  const [action, setAction] = useState<ProfileLifecycleAction>()
  const [reason, setReason] = useState('')

  useEffect(() => {
    if (!action) return
    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === 'Escape' && !isProcessing) setAction(undefined)
    }
    window.addEventListener('keydown', closeOnEscape)
    return () => window.removeEventListener('keydown', closeOnEscape)
  }, [action, isProcessing])

  const isSuspended = status === 'SUSPENDED'
  const canSuspend = status === 'APPROVED' || status === 'PUBLISHED'
  if (!isSuspended && !canSuspend) return null

  const normalizedReason = reason.trim()
  const isReasonValid = normalizedReason.length > 0 && normalizedReason.length <= REASON_MAX_LENGTH
  const isSuspendAction = action === 'suspend'

  function close() {
    if (isProcessing) return
    setAction(undefined)
    setReason('')
  }

  function confirm() {
    if (!action || !isReasonValid) return
    onConfirm(action, normalizedReason)
  }

  return (
    <>
      <section className="mt-6 flex flex-col gap-5 rounded-[26px] border border-[#292929] bg-[#111] p-6 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-xs font-bold tracking-[.14em] text-[#777]">MODERAÇÃO DO PERFIL</p>
          <h2 className="mt-2 font-['Manrope'] text-xl font-extrabold">
            {isSuspended ? 'Perfil suspenso' : 'Perfil elegível para suspensão'}
          </h2>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-[#aaa]">
            {isSuspended
              ? 'Reative somente após validar que o perfil pode retornar ao estado anterior.'
              : 'A suspensão remove imediatamente o perfil do catálogo público quando ele estiver publicado.'}
          </p>
        </div>
        <button
          className={`shrink-0 rounded-full px-5 py-3 text-sm font-extrabold transition ${
            isSuspended
              ? 'bg-[#c7ff3d] text-[#080808] hover:bg-[#d6ff70]'
              : 'border border-[#ff6b6b]/60 text-[#ff8a8a] hover:bg-[#ff6b6b]/10'
          }`}
          onClick={() => setAction(isSuspended ? 'reactivate' : 'suspend')}
          type="button">
          {isSuspended ? 'Reativar perfil' : 'Suspender perfil'}
        </button>
      </section>

      {action && (
        <div
          aria-labelledby="lifecycle-dialog-title"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-5 backdrop-blur-sm"
          role="dialog">
          <div className="w-full max-w-xl rounded-[28px] border border-[#333] bg-[#111] p-6 shadow-2xl md:p-7">
            <p className={`text-xs font-bold tracking-[.14em] ${isSuspendAction ? 'text-[#ff8a8a]' : 'text-[#c7ff3d]'}`}>
              CONFIRMAR AÇÃO
            </p>
            <h2 className="mt-3 font-['Manrope'] text-2xl font-extrabold" id="lifecycle-dialog-title">
              {isSuspendAction ? `Suspender ${profileName}?` : `Reativar ${profileName}?`}
            </h2>
            <p className="mt-3 text-sm leading-6 text-[#aaa]">
              {isSuspendAction
                ? 'O perfil deixará de aparecer no catálogo até que seja reativado.'
                : 'O perfil retornará ao status que possuía antes da suspensão.'}
            </p>

            <label className="mt-6 block text-sm font-bold" htmlFor="lifecycle-reason">
              {isSuspendAction ? 'Motivo da suspensão' : 'Motivo da reativação'}
            </label>
            <textarea
              aria-describedby="lifecycle-reason-counter"
              autoFocus
              className="mt-2 min-h-28 w-full resize-y rounded-xl border border-[#333] bg-[#080808] px-4 py-3 text-sm outline-none transition placeholder:text-[#666] focus:border-[#7657ff] disabled:cursor-wait disabled:opacity-55"
              disabled={isProcessing}
              id="lifecycle-reason"
              maxLength={REASON_MAX_LENGTH}
              onChange={(event) => setReason(event.target.value)}
              placeholder="Registre uma justificativa clara para a auditoria"
              value={reason}
            />
            <p className="mt-1 text-right text-xs text-[#777]" id="lifecycle-reason-counter">
              {reason.length}/{REASON_MAX_LENGTH}
            </p>

            <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <button
                className="rounded-xl border border-[#444] px-5 py-3 text-sm font-bold transition hover:border-[#777] disabled:opacity-40"
                disabled={isProcessing}
                onClick={close}
                type="button">
                Cancelar
              </button>
              <button
                className={`rounded-xl px-5 py-3 text-sm font-extrabold transition disabled:cursor-not-allowed disabled:opacity-35 ${
                  isSuspendAction
                    ? 'bg-[#ff6b6b] text-[#080808] hover:bg-[#ff8a8a]'
                    : 'bg-[#c7ff3d] text-[#080808] hover:bg-[#d6ff70]'
                }`}
                disabled={isProcessing || !isReasonValid}
                onClick={confirm}
                type="button">
                {isProcessing
                  ? isSuspendAction
                    ? 'Suspendendo…'
                    : 'Reativando…'
                  : isSuspendAction
                    ? 'Confirmar suspensão'
                    : 'Confirmar reativação'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
