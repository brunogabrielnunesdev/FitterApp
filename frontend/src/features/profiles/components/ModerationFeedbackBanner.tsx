import type { ModerationFeedback } from '../types/moderation'

type ModerationFeedbackBannerProps = {
  feedback: ModerationFeedback
  onDismiss: () => void
}

export function ModerationFeedbackBanner({ feedback, onDismiss }: ModerationFeedbackBannerProps) {
  const isSuccess = feedback.status === 'success'

  return (
    <div
      aria-live="polite"
      className={`mb-6 flex items-start justify-between gap-4 rounded-2xl border px-5 py-4 text-sm ${
        isSuccess
          ? 'border-[#c7ff3d]/40 bg-[#c7ff3d]/10 text-[#e8ffad]'
          : 'border-[#ff6b6b]/40 bg-[#ff6b6b]/10 text-[#ffb0b0]'
      }`}
      role={isSuccess ? 'status' : 'alert'}>
      <div>
        <p className="font-bold">{isSuccess ? 'Operação concluída' : 'A operação falhou'}</p>
        <p className="mt-1 text-[#f6f4ee]">{feedback.message}</p>
      </div>
      <button
        aria-label="Fechar mensagem"
        className="shrink-0 rounded-lg px-2 py-1 font-bold transition hover:bg-white/10"
        onClick={onDismiss}
        type="button">
        Fechar
      </button>
    </div>
  )
}
