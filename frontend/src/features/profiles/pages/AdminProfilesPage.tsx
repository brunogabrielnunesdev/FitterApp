import { useQuery } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'

import {
  listAdminProfiles,
  paginateProfiles,
  searchAdminProfiles,
} from '../services/adminProfileService'
import { profileQueryKeys } from '../services/profileQueryKeys'
import type { ProfileStatus } from '../types/profileDetail'
import type { ProfileStatusFilter } from '../types/profileManagement'
import { getModerationErrorMessage } from '../utils/getModerationErrorMessage'

const PAGE_SIZE = 20

const statusOptions: Array<{ value: ProfileStatusFilter; label: string }> = [
  { value: 'ALL', label: 'Todos os status' },
  { value: 'DRAFT', label: 'Rascunho' },
  { value: 'PENDING_REVIEW', label: 'Aguardando análise' },
  { value: 'APPROVED', label: 'Aprovado' },
  { value: 'PUBLISHED', label: 'Publicado' },
  { value: 'REJECTED', label: 'Reprovado' },
  { value: 'SUSPENDED', label: 'Suspenso' },
]

const statusLabels: Record<ProfileStatus, string> = {
  DRAFT: 'Rascunho',
  PENDING_REVIEW: 'Aguardando análise',
  APPROVED: 'Aprovado',
  PUBLISHED: 'Publicado',
  REJECTED: 'Reprovado',
  SUSPENDED: 'Suspenso',
}

const statusStyles: Record<ProfileStatus, string> = {
  DRAFT: 'border-[#555]/50 bg-[#555]/10 text-[#aaa]',
  PENDING_REVIEW: 'border-[#7657ff]/45 bg-[#7657ff]/10 text-[#b5a9ff]',
  APPROVED: 'border-[#8ccf3f]/40 bg-[#8ccf3f]/10 text-[#bde681]',
  PUBLISHED: 'border-[#c7ff3d]/40 bg-[#c7ff3d]/10 text-[#dfff8d]',
  REJECTED: 'border-[#ff9f43]/40 bg-[#ff9f43]/10 text-[#ffc27e]',
  SUSPENDED: 'border-[#ff6b6b]/40 bg-[#ff6b6b]/10 text-[#ff9b9b]',
}

function formatDate(value: string | null) {
  if (!value) return 'Não enviado'
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' }).format(new Date(value))
}

function ProfilesLoadingState() {
  return (
    <div aria-live="polite" className="grid gap-3" role="status">
      <span className="sr-only">Carregando perfis</span>
      {[0, 1, 2, 3].map((item) => (
        <div
          className="h-32 animate-pulse rounded-[24px] border border-[#292929] bg-[#111]"
          key={item}
        />
      ))}
    </div>
  )
}

export function AdminProfilesPage() {
  const [status, setStatus] = useState<ProfileStatusFilter>('ALL')
  const [page, setPage] = useState(0)
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const queryPage = search ? 0 : page
  const profilesQuery = useQuery({
    queryKey: profileQueryKeys.list(status, queryPage, search),
    queryFn: async () => {
      if (search) return { kind: 'search' as const, profiles: await searchAdminProfiles(status, search) }
      return {
        kind: 'page' as const,
        result: await listAdminProfiles({ status, page, size: PAGE_SIZE }),
      }
    },
    retry: false,
  })

  function submitSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setPage(0)
    setSearch(searchInput.trim())
  }

  function clearSearch() {
    setSearchInput('')
    setSearch('')
    setPage(0)
  }

  const queryData = profilesQuery.data
  const profiles =
    queryData?.kind === 'search'
      ? paginateProfiles(queryData.profiles, page, PAGE_SIZE)
      : (queryData?.result.content ?? [])
  const totalElements =
    queryData?.kind === 'search'
      ? queryData.profiles.length
      : (queryData?.result.totalElements ?? 0)
  const totalPages =
    queryData?.kind === 'search'
      ? Math.ceil(queryData.profiles.length / PAGE_SIZE)
      : (queryData?.result.totalPages ?? 0)

  return (
    <div className="mx-auto max-w-[1200px] px-6 py-10 lg:px-10 lg:py-14">
      <div className="flex flex-col gap-5 border-b border-[#292929] pb-8 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="text-xs font-bold tracking-[.18em] text-[#c7ff3d]">GESTÃO DE PERFIS</p>
          <h1 className="mt-3 font-['Manrope'] text-4xl font-extrabold tracking-[-.045em] md:text-5xl">
            Perfis profissionais
          </h1>
          <p className="mt-3 max-w-2xl text-[#aaaaaa]">
            Localize, consulte e modere os personais em todas as etapas do cadastro.
          </p>
        </div>
        <Link
          className="w-fit shrink-0 rounded-full border border-[#7657ff]/50 bg-[#7657ff]/10 px-5 py-3 text-sm font-bold text-[#b5a9ff] transition hover:border-[#9a84ff]"
          to="/admin/personals/pending">
          Abrir fila de análise
        </Link>
      </div>

      <section aria-label="Filtros de perfis" className="mt-8 rounded-[24px] border border-[#292929] bg-[#111] p-5">
        <form className="grid gap-4 md:grid-cols-[1fr_240px_auto] md:items-end" onSubmit={submitSearch}>
          <div>
            <label className="text-xs font-bold tracking-[.08em] text-[#aaa] uppercase" htmlFor="profile-search">
              Buscar por nome
            </label>
            <input
              className="mt-2 h-12 w-full rounded-xl border border-[#333] bg-[#171717] px-4 text-sm outline-none transition placeholder:text-[#666] focus:border-[#7657ff]"
              id="profile-search"
              onChange={(event) => setSearchInput(event.target.value)}
              placeholder="Nome do personal"
              value={searchInput}
            />
          </div>
          <div>
            <label className="text-xs font-bold tracking-[.08em] text-[#aaa] uppercase" htmlFor="profile-status">
              Status
            </label>
            <select
              className="mt-2 h-12 w-full rounded-xl border border-[#333] bg-[#171717] px-4 text-sm outline-none transition focus:border-[#7657ff]"
              id="profile-status"
              onChange={(event) => {
                setStatus(event.target.value as ProfileStatusFilter)
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
          <div className="flex gap-2">
            {search && (
              <button
                className="h-12 rounded-xl border border-[#444] px-4 text-sm font-bold text-[#aaa] transition hover:border-[#777] hover:text-[#f6f4ee]"
                onClick={clearSearch}
                type="button">
                Limpar
              </button>
            )}
            <button
              className="h-12 flex-1 rounded-xl bg-[#f6f4ee] px-5 text-sm font-extrabold text-[#080808] transition hover:bg-[#c7ff3d] md:flex-none"
              type="submit">
              Buscar
            </button>
          </div>
        </form>
        {search && (
          <p className="mt-4 text-xs text-[#888]">
            Exibindo resultados para <strong className="text-[#d6d2ca]">“{search}”</strong>
          </p>
        )}
      </section>

      <section aria-label="Resultados de perfis" className="mt-6">
        {profilesQuery.isPending && <ProfilesLoadingState />}
        {profilesQuery.isError && (
          <div className="rounded-[24px] border border-[#ff6b6b]/40 bg-[#ff6b6b]/5 p-8 text-center" role="alert">
            <h2 className="font-['Manrope'] text-xl font-extrabold">Não foi possível carregar os perfis</h2>
            <p className="mx-auto mt-2 max-w-lg text-sm text-[#aaa]">
              {getModerationErrorMessage(profilesQuery.error, 'carregar')}
            </p>
            <button
              className="mt-5 rounded-full bg-[#f6f4ee] px-5 py-2.5 text-sm font-bold text-[#080808] disabled:opacity-50"
              disabled={profilesQuery.isFetching}
              onClick={() => void profilesQuery.refetch()}
              type="button">
              {profilesQuery.isFetching ? 'Tentando novamente…' : 'Tentar novamente'}
            </button>
          </div>
        )}
        {profilesQuery.isSuccess && profiles.length === 0 && (
          <div className="rounded-[24px] border border-[#292929] bg-[#111] p-8 text-center" role="status">
            <h2 className="font-['Manrope'] text-xl font-extrabold">Nenhum perfil encontrado</h2>
            <p className="mt-2 text-sm text-[#aaa]">
              {search ? 'Tente outro nome ou ajuste o status selecionado.' : 'Não há perfis com este status.'}
            </p>
          </div>
        )}
        {profilesQuery.isSuccess && profiles.length > 0 && (
          <>
            <div className="mb-3 flex items-center justify-between text-sm text-[#888]">
              <p>{totalElements === 1 ? '1 perfil encontrado' : `${totalElements} perfis encontrados`}</p>
              {totalPages > 0 && <p>Página {page + 1} de {totalPages}</p>}
            </div>
            <div className="grid gap-3">
              {profiles.map((profile) => (
                <article
                  className="rounded-[24px] border border-[#292929] bg-[#111] p-5 transition hover:border-[#444] md:p-6"
                  key={profile.profileId}>
                  <div className="flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className={`rounded-full border px-3 py-1 text-[11px] font-bold tracking-[.08em] ${statusStyles[profile.profileStatus]}`}>
                          {statusLabels[profile.profileStatus]}
                        </span>
                        {profile.published && profile.profileStatus !== 'PUBLISHED' && (
                          <span className="rounded-full border border-[#333] px-3 py-1 text-[11px] font-bold text-[#999]">
                            VERSÃO PÚBLICA ATIVA
                          </span>
                        )}
                      </div>
                      <h2 className="mt-3 truncate font-['Manrope'] text-xl font-extrabold">{profile.fullName}</h2>
                      <p className="mt-1 truncate text-sm text-[#888]">{profile.email}</p>
                    </div>
                    <div className="flex shrink-0 flex-col gap-3 sm:flex-row sm:items-center">
                      <dl className="grid grid-cols-2 gap-x-5 text-xs sm:text-right">
                        <div>
                          <dt className="text-[#666]">Enviado</dt>
                          <dd className="mt-1 text-[#aaa]">{formatDate(profile.submittedAt)}</dd>
                        </div>
                        <div>
                          <dt className="text-[#666]">Atualizado</dt>
                          <dd className="mt-1 text-[#aaa]">{formatDate(profile.updatedAt)}</dd>
                        </div>
                      </dl>
                      <Link
                        className="rounded-full bg-[#f6f4ee] px-5 py-2.5 text-center text-sm font-extrabold text-[#080808] transition hover:bg-[#c7ff3d]"
                        state={{ from: '/admin/personals' }}
                        to={`/admin/personals/${profile.profileId}`}>
                        Abrir detalhe
                      </Link>
                    </div>
                  </div>
                </article>
              ))}
            </div>

            {totalPages > 1 && (
              <nav aria-label="Paginação de perfis" className="mt-6 flex items-center justify-between gap-4">
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
