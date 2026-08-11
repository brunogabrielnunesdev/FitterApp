type QueueStateProps = {
  status: 'loading' | 'empty' | 'error'
  message?: string
  onRetry?: () => void
  isRetrying?: boolean
}

export function QueueState({ status, message, onRetry, isRetrying }: QueueStateProps) {
  if (status === 'loading') {
    return (
      <div aria-live="polite" className="grid gap-4" role="status">
        <span className="sr-only">Carregando fila de moderação</span>
        {[0, 1, 2].map((item) => (
          <div
            className="h-48 animate-pulse rounded-[26px] border border-[#292929] bg-[#111]"
            key={item}
          />
        ))}
      </div>
    )
  }

  return (
    <div
      className={`rounded-[26px] border p-8 text-center ${
        status === 'error'
          ? 'border-[#ff6b6b]/40 bg-[#ff6b6b]/5'
          : 'border-[#292929] bg-[#111]'
      }`}
      role={status === 'error' ? 'alert' : 'status'}>
      <p className="font-['Manrope'] text-xl font-extrabold">
        {status === 'error' ? 'A fila não pôde ser carregada' : 'Fila em dia'}
      </p>
      <p className="mx-auto mt-2 max-w-lg text-sm leading-6 text-[#aaaaaa]">
        {message ?? 'Nenhum perfil está aguardando análise neste momento.'}
      </p>
      {status === 'error' && onRetry && (
        <button
          className="mt-5 rounded-full bg-[#f6f4ee] px-5 py-2.5 text-sm font-bold text-[#080808] transition hover:bg-[#c7ff3d] disabled:cursor-wait disabled:opacity-60"
          disabled={isRetrying}
          onClick={onRetry}
          type="button">
          {isRetrying ? 'Tentando novamente…' : 'Tentar novamente'}
        </button>
      )}
    </div>
  )
}
