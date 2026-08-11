type ProfileDetailStateProps =
  | { status: 'loading' }
  | { status: 'error'; message: string; isRetrying: boolean; onRetry: () => void }

export function ProfileDetailState(props: ProfileDetailStateProps) {
  if (props.status === 'loading') {
    return (
      <div aria-live="polite" className="rounded-[26px] border border-[#292929] bg-[#111] p-8">
        <div className="h-4 w-32 animate-pulse rounded bg-[#292929]" />
        <div className="mt-5 h-10 max-w-lg animate-pulse rounded bg-[#202020]" />
        <div className="mt-8 grid gap-4 md:grid-cols-2">
          <div className="h-48 animate-pulse rounded-2xl bg-[#181818]" />
          <div className="h-48 animate-pulse rounded-2xl bg-[#181818]" />
        </div>
        <span className="sr-only">Carregando detalhes do perfil</span>
      </div>
    )
  }

  return (
    <div className="rounded-[26px] border border-[#ff6b6b]/35 bg-[#ff6b6b]/8 p-8">
      <p className="font-['Manrope'] text-xl font-extrabold">Não foi possível abrir o perfil</p>
      <p className="mt-2 max-w-xl text-sm leading-6 text-[#c2b8b8]">{props.message}</p>
      <button
        className="mt-6 rounded-full border border-[#ff8a8a]/60 px-5 py-2.5 text-sm font-bold text-[#ff9b9b] transition hover:bg-[#ff6b6b]/10 disabled:cursor-wait disabled:opacity-50"
        disabled={props.isRetrying}
        onClick={props.onRetry}
        type="button">
        {props.isRetrying ? 'Tentando novamente…' : 'Tentar novamente'}
      </button>
    </div>
  )
}
