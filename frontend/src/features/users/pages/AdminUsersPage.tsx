import { useQuery } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'

import { listAdminUsers } from '../services/adminUserService'
import { userQueryKeys } from '../services/userQueryKeys'
import type { UserRoleFilter, UserStatusFilter } from '../types/adminUser'
import { getAdminUserErrorMessage } from '../utils/getAdminUserErrorMessage'
import { userRoleLabels, userStatusLabels, userStatusStyles } from '../utils/userLabels'

const PAGE_SIZE = 20

const statusOptions: Array<{ value: UserStatusFilter; label: string }> = [
  { value: 'ALL', label: 'Todos os status' },
  { value: 'ACTIVE', label: 'Ativa' },
  { value: 'PENDING_VERIFICATION', label: 'Verificação pendente' },
  { value: 'BLOCKED', label: 'Bloqueada' },
]

const roleOptions: Array<{ value: UserRoleFilter; label: string }> = [
  { value: 'ALL', label: 'Todas as permissões' },
  { value: 'STUDENT', label: 'Aluno' },
  { value: 'PERSONAL', label: 'Personal' },
  { value: 'ADMIN', label: 'Administrador' },
  { value: 'OWNER', label: 'Proprietário' },
]

function formatDate(value: string) {
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' }).format(new Date(value))
}

function UsersLoadingState() {
  return (
    <div aria-live="polite" className="grid gap-3" role="status">
      <span className="sr-only">Carregando usuários</span>
      {[0, 1, 2, 3].map((item) => (
        <div
          className="h-32 animate-pulse rounded-[24px] border border-[#292929] bg-[#111]"
          key={item}
        />
      ))}
    </div>
  )
}

export function AdminUsersPage() {
  const [queryInput, setQueryInput] = useState('')
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState<UserStatusFilter>('ALL')
  const [role, setRole] = useState<UserRoleFilter>('ALL')
  const [page, setPage] = useState(0)
  const usersQuery = useQuery({
    queryKey: userQueryKeys.list(query, status, role, page),
    queryFn: () => listAdminUsers({ query, status, role, page, size: PAGE_SIZE }),
    retry: false,
  })

  function submitSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setPage(0)
    setQuery(queryInput.trim())
  }

  function clearFilters() {
    setQueryInput('')
    setQuery('')
    setStatus('ALL')
    setRole('ALL')
    setPage(0)
  }

  const hasFilters = Boolean(query) || status !== 'ALL' || role !== 'ALL'
  const users = usersQuery.data?.content ?? []
  const totalElements = usersQuery.data?.totalElements ?? 0
  const totalPages = usersQuery.data?.totalPages ?? 0

  return (
    <div className="mx-auto max-w-[1200px] px-6 py-10 lg:px-10 lg:py-14">
      <div className="border-b border-[#292929] pb-8">
        <p className="text-xs font-bold tracking-[.18em] text-[#c7ff3d]">GESTÃO DE USUÁRIOS</p>
        <h1 className="mt-3 font-['Manrope'] text-4xl font-extrabold tracking-[-.045em] md:text-5xl">
          Contas e permissões
        </h1>
        <p className="mt-3 max-w-2xl text-[#aaaaaa]">
          Localize contas pelo nome ou e-mail e consulte seus estados e contextos de acesso.
        </p>
      </div>

      <section aria-label="Filtros de usuários" className="mt-8 rounded-[24px] border border-[#292929] bg-[#111] p-5">
        <form className="grid gap-4 lg:grid-cols-[1fr_200px_220px_auto] lg:items-end" onSubmit={submitSearch}>
          <div>
            <label className="text-xs font-bold tracking-[.08em] text-[#aaa] uppercase" htmlFor="user-search">
              Nome ou e-mail
            </label>
            <input
              className="mt-2 h-12 w-full rounded-xl border border-[#333] bg-[#171717] px-4 text-sm outline-none transition placeholder:text-[#666] focus:border-[#7657ff]"
              id="user-search"
              onChange={(event) => setQueryInput(event.target.value)}
              placeholder="Buscar conta"
              value={queryInput}
            />
          </div>
          <div>
            <label className="text-xs font-bold tracking-[.08em] text-[#aaa] uppercase" htmlFor="user-status">
              Status
            </label>
            <select
              className="mt-2 h-12 w-full rounded-xl border border-[#333] bg-[#171717] px-4 text-sm outline-none transition focus:border-[#7657ff]"
              id="user-status"
              onChange={(event) => {
                setStatus(event.target.value as UserStatusFilter)
                setPage(0)
              }}
              value={status}>
              {statusOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-xs font-bold tracking-[.08em] text-[#aaa] uppercase" htmlFor="user-role">
              Permissão
            </label>
            <select
              className="mt-2 h-12 w-full rounded-xl border border-[#333] bg-[#171717] px-4 text-sm outline-none transition focus:border-[#7657ff]"
              id="user-role"
              onChange={(event) => {
                setRole(event.target.value as UserRoleFilter)
                setPage(0)
              }}
              value={role}>
              {roleOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div className="flex gap-2">
            {hasFilters && (
              <button
                className="h-12 rounded-xl border border-[#444] px-4 text-sm font-bold text-[#aaa] transition hover:border-[#777] hover:text-[#f6f4ee]"
                onClick={clearFilters}
                type="button">
                Limpar
              </button>
            )}
            <button
              className="h-12 flex-1 rounded-xl bg-[#f6f4ee] px-5 text-sm font-extrabold text-[#080808] transition hover:bg-[#c7ff3d] lg:flex-none"
              type="submit">
              Buscar
            </button>
          </div>
        </form>
      </section>

      <section aria-label="Resultados de usuários" className="mt-6">
        {usersQuery.isPending && <UsersLoadingState />}
        {usersQuery.isError && (
          <div className="rounded-[24px] border border-[#ff6b6b]/40 bg-[#ff6b6b]/5 p-8 text-center" role="alert">
            <h2 className="font-['Manrope'] text-xl font-extrabold">Não foi possível carregar os usuários</h2>
            <p className="mx-auto mt-2 max-w-lg text-sm text-[#aaa]">
              {getAdminUserErrorMessage(usersQuery.error, 'list')}
            </p>
            <button
              className="mt-5 rounded-full bg-[#f6f4ee] px-5 py-2.5 text-sm font-bold text-[#080808] disabled:opacity-50"
              disabled={usersQuery.isFetching}
              onClick={() => void usersQuery.refetch()}
              type="button">
              {usersQuery.isFetching ? 'Tentando novamente…' : 'Tentar novamente'}
            </button>
          </div>
        )}
        {usersQuery.isSuccess && users.length === 0 && (
          <div className="rounded-[24px] border border-[#292929] bg-[#111] p-8 text-center" role="status">
            <h2 className="font-['Manrope'] text-xl font-extrabold">Nenhuma conta encontrada</h2>
            <p className="mt-2 text-sm text-[#aaa]">Tente ajustar a busca ou os filtros selecionados.</p>
          </div>
        )}
        {usersQuery.isSuccess && users.length > 0 && (
          <>
            <div className="mb-3 flex items-center justify-between text-sm text-[#888]">
              <p>{totalElements === 1 ? '1 conta encontrada' : `${totalElements} contas encontradas`}</p>
              {totalPages > 0 && <p>Página {page + 1} de {totalPages}</p>}
            </div>
            <div className="grid gap-3">
              {users.map((user) => (
                <article
                  className="rounded-[24px] border border-[#292929] bg-[#111] p-5 transition hover:border-[#444] md:p-6"
                  key={user.userId}>
                  <div className="flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className={`rounded-full border px-3 py-1 text-[11px] font-bold tracking-[.08em] ${userStatusStyles[user.status]}`}>
                          {userStatusLabels[user.status]}
                        </span>
                        {user.roles.map((assignedRole) => (
                          <span
                            className="rounded-full border border-[#7657ff]/35 bg-[#7657ff]/10 px-3 py-1 text-[11px] font-bold text-[#b5a9ff]"
                            key={assignedRole.name}>
                            {userRoleLabels[assignedRole.name]}
                          </span>
                        ))}
                      </div>
                      <h2 className="mt-3 truncate font-['Manrope'] text-xl font-extrabold">{user.fullName}</h2>
                      <p className="mt-1 truncate text-sm text-[#888]">{user.email}</p>
                    </div>
                    <div className="flex shrink-0 flex-col gap-3 sm:flex-row sm:items-center">
                      <div className="text-xs sm:text-right">
                        <p className="text-[#666]">Conta criada</p>
                        <p className="mt-1 text-[#aaa]">{formatDate(user.createdAt)}</p>
                      </div>
                      <Link
                        className="rounded-full bg-[#f6f4ee] px-5 py-2.5 text-center text-sm font-extrabold text-[#080808] transition hover:bg-[#c7ff3d]"
                        to={`/admin/users/${user.userId}`}>
                        Abrir detalhe
                      </Link>
                    </div>
                  </div>
                </article>
              ))}
            </div>

            {totalPages > 1 && (
              <nav aria-label="Paginação de usuários" className="mt-6 flex items-center justify-between gap-4">
                <button
                  className="rounded-full border border-[#333] px-5 py-2.5 text-sm font-bold transition hover:border-[#777] disabled:cursor-not-allowed disabled:opacity-35"
                  disabled={page === 0}
                  onClick={() => setPage((current) => Math.max(current - 1, 0))}
                  type="button">
                  Anterior
                </button>
                <span className="text-sm text-[#888]">{page + 1} / {totalPages}</span>
                <button
                  className="rounded-full border border-[#333] px-5 py-2.5 text-sm font-bold transition hover:border-[#777] disabled:cursor-not-allowed disabled:opacity-35"
                  disabled={page + 1 >= totalPages}
                  onClick={() => setPage((current) => current + 1)}
                  type="button">
                  Próxima
                </button>
              </nav>
            )}
          </>
        )}
      </section>
    </div>
  )
}
