import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'

import { getAdminUser } from '../services/adminUserService'
import { userQueryKeys } from '../services/userQueryKeys'
import { getAdminUserErrorMessage } from '../utils/getAdminUserErrorMessage'
import { userRoleLabels, userStatusLabels, userStatusStyles } from '../utils/userLabels'

function formatDate(value: string | null) {
  if (!value) return 'Não informado'
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'long',
    timeStyle: 'short',
  }).format(new Date(value))
}

function DataItem({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <dt className="text-xs font-bold tracking-[.08em] text-[#777] uppercase">{label}</dt>
      <dd className="mt-1.5 break-words text-sm leading-6 text-[#e3e0d9]">{children}</dd>
    </div>
  )
}

function UserDetailLoadingState() {
  return (
    <div aria-live="polite" className="rounded-[26px] border border-[#292929] bg-[#111] p-8" role="status">
      <span className="sr-only">Carregando detalhes da conta</span>
      <div className="h-5 w-32 animate-pulse rounded bg-[#292929]" />
      <div className="mt-5 h-10 max-w-md animate-pulse rounded bg-[#202020]" />
      <div className="mt-8 grid gap-4 md:grid-cols-2">
        <div className="h-52 animate-pulse rounded-2xl bg-[#181818]" />
        <div className="h-52 animate-pulse rounded-2xl bg-[#181818]" />
      </div>
    </div>
  )
}

export function AdminUserDetailPage() {
  const { userId = '' } = useParams()
  const userQuery = useQuery({
    queryKey: userQueryKeys.detail(userId),
    queryFn: () => getAdminUser(userId),
    enabled: Boolean(userId),
    retry: false,
  })
  const user = userQuery.data

  return (
    <div className="mx-auto max-w-5xl px-6 py-10 lg:px-10 lg:py-14">
      <div className="flex flex-col gap-5 border-b border-[#292929] pb-8 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-bold tracking-[.18em] text-[#c7ff3d]">DETALHE DA CONTA</p>
          <h1 className="mt-3 font-['Manrope'] text-4xl font-extrabold tracking-[-.045em] md:text-5xl">
            {user?.fullName ?? 'Dados do usuário'}
          </h1>
          <p className="mt-3 max-w-2xl text-[#aaaaaa]">
            Estado cadastral, contatos e permissões vinculadas à conta.
          </p>
        </div>
        <Link
          className="w-fit shrink-0 rounded-full border border-[#333] px-5 py-2.5 text-sm font-bold transition hover:border-[#c7ff3d] hover:text-[#c7ff3d]"
          to="/admin/users">
          Voltar aos usuários
        </Link>
      </div>

      <div className="mt-8">
        {userQuery.isPending && <UserDetailLoadingState />}
        {userQuery.isError && (
          <div className="rounded-[26px] border border-[#ff6b6b]/40 bg-[#ff6b6b]/5 p-8" role="alert">
            <h2 className="font-['Manrope'] text-xl font-extrabold">Não foi possível abrir esta conta</h2>
            <p className="mt-2 max-w-xl text-sm leading-6 text-[#c2b8b8]">
              {getAdminUserErrorMessage(userQuery.error, 'detail')}
            </p>
            <button
              className="mt-6 rounded-full bg-[#f6f4ee] px-5 py-2.5 text-sm font-bold text-[#080808] disabled:opacity-50"
              disabled={userQuery.isFetching}
              onClick={() => void userQuery.refetch()}
              type="button">
              {userQuery.isFetching ? 'Tentando novamente…' : 'Tentar novamente'}
            </button>
          </div>
        )}

        {user && (
          <div className="grid gap-5 md:grid-cols-2">
            <section className="rounded-[26px] border border-[#292929] bg-[#111] p-6 md:p-7">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="text-[11px] font-bold tracking-[.15em] text-[#777]">CADASTRO</p>
                  <h2 className="mt-2 font-['Manrope'] text-xl font-extrabold">Dados da conta</h2>
                </div>
                <span className={`rounded-full border px-3 py-1.5 text-[11px] font-bold tracking-[.08em] ${userStatusStyles[user.status]}`}>
                  {userStatusLabels[user.status]}
                </span>
              </div>
              <dl className="mt-6 grid gap-5 sm:grid-cols-2">
                <DataItem label="Nome completo">{user.fullName}</DataItem>
                <DataItem label="Status">{userStatusLabels[user.status]}</DataItem>
                <DataItem label="E-mail">
                  <a className="hover:text-[#c7ff3d]" href={`mailto:${user.email}`}>
                    {user.email}
                  </a>
                </DataItem>
                <DataItem label="Telefone">
                  <a className="hover:text-[#c7ff3d]" href={`tel:${user.phoneNumber}`}>
                    {user.phoneNumber}
                  </a>
                </DataItem>
                <DataItem label="Verificação do e-mail">
                  {user.emailVerifiedAt ? `Confirmado em ${formatDate(user.emailVerifiedAt)}` : 'Pendente'}
                </DataItem>
              </dl>
            </section>

            <section className="rounded-[26px] border border-[#292929] bg-[#111] p-6 md:p-7">
              <p className="text-[11px] font-bold tracking-[.15em] text-[#777]">ACESSO</p>
              <h2 className="mt-2 font-['Manrope'] text-xl font-extrabold">Permissões e contextos</h2>
              {user.roles.length > 0 ? (
                <ul className="mt-6 grid gap-3">
                  {user.roles.map((role) => (
                    <li className="rounded-xl border border-[#7657ff]/30 bg-[#7657ff]/8 p-4" key={role.name}>
                      <div className="flex items-center justify-between gap-3">
                        <p className="font-bold text-[#c7c0ff]">{userRoleLabels[role.name]}</p>
                        <span className="rounded-full border border-[#7657ff]/30 px-2.5 py-1 text-[10px] font-bold tracking-[.08em] text-[#9d8dff]">
                          {role.name}
                        </span>
                      </div>
                      <p className="mt-2 text-xs leading-5 text-[#888]">
                        Concedida em {formatDate(role.grantedAt)} · {role.grantedByUserId ? 'por uma conta administrativa' : 'pelo sistema'}
                      </p>
                    </li>
                  ))}
                </ul>
              ) : (
                <div className="mt-6 rounded-xl border border-[#333] bg-[#171717] p-5">
                  <p className="font-bold">Nenhuma permissão atribuída</p>
                  <p className="mt-1 text-sm text-[#888]">A conta não possui um contexto de acesso ativo.</p>
                </div>
              )}
            </section>

            <section className="rounded-[26px] border border-[#292929] bg-[#111] p-6 md:col-span-2 md:p-7">
              <p className="text-[11px] font-bold tracking-[.15em] text-[#777]">HISTÓRICO</p>
              <h2 className="mt-2 font-['Manrope'] text-xl font-extrabold">Linha do tempo cadastral</h2>
              <dl className="mt-6 grid gap-5 sm:grid-cols-3">
                <DataItem label="Conta criada">{formatDate(user.createdAt)}</DataItem>
                <DataItem label="Última atualização">{formatDate(user.updatedAt)}</DataItem>
                <DataItem label="E-mail verificado">{formatDate(user.emailVerifiedAt)}</DataItem>
              </dl>
            </section>

            <aside className="rounded-2xl border border-[#333] bg-[#171717] p-5 text-sm leading-6 text-[#888] md:col-span-2">
              Esta visualização apresenta somente dados cadastrais autorizados pelo contrato administrativo. Credenciais, tokens e informações internas de autenticação não são expostos.
            </aside>
          </div>
        )}
      </div>
    </div>
  )
}
