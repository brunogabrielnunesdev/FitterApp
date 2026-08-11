import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'

import {
  createAdminModality,
  listAdminModalities,
  setAdminModalityActive,
  updateAdminModality,
} from '../services/adminModalityService'
import { modalityQueryKeys } from '../services/modalityQueryKeys'
import type { AdminModality, ModalityStatusFilter } from '../types/modality'
import { getModalityErrorMessage } from '../utils/getModalityErrorMessage'

type Feedback = { status: 'success' | 'error'; message: string }
const EMPTY_MODALITIES: AdminModality[] = []

function normalizeName(value: string) {
  return value.trim().replace(/\s+/g, ' ')
}

function validateName(value: string) {
  const normalized = normalizeName(value)
  if (!normalized) return 'Informe o nome da modalidade.'
  if (normalized.length > 80) return 'Use no máximo 80 caracteres.'
  return null
}

export default function AdminModalitiesPage() {
  const queryClient = useQueryClient()
  const [newName, setNewName] = useState('')
  const [createError, setCreateError] = useState<string>()
  const [editing, setEditing] = useState<AdminModality>()
  const [editName, setEditName] = useState('')
  const [editError, setEditError] = useState<string>()
  const [pendingActivation, setPendingActivation] = useState<AdminModality>()
  const [statusFilter, setStatusFilter] = useState<ModalityStatusFilter>('ALL')
  const [feedback, setFeedback] = useState<Feedback>()
  const modalitiesQuery = useQuery({
    queryKey: modalityQueryKeys.all,
    queryFn: listAdminModalities,
    retry: false,
  })
  const createMutation = useMutation({ mutationFn: createAdminModality })
  const editMutation = useMutation({
    mutationFn: ({ id, name }: { id: number; name: string }) => updateAdminModality(id, name),
  })
  const activationMutation = useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) =>
      setAdminModalityActive(id, active),
  })

  const modalities = modalitiesQuery.data ?? EMPTY_MODALITIES
  const activeCount = modalities.filter((modality) => modality.active).length
  const filteredModalities = modalities.filter((modality) => {
    if (statusFilter === 'ACTIVE') return modality.active
    if (statusFilter === 'INACTIVE') return !modality.active
    return true
  })

  async function refreshModalities() {
    await queryClient.invalidateQueries({ queryKey: modalityQueryKeys.all })
  }

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const validationError = validateName(newName)
    setCreateError(validationError ?? undefined)
    if (validationError) return
    setFeedback(undefined)
    try {
      const created = await createMutation.mutateAsync(normalizeName(newName))
      setNewName('')
      setFeedback({ status: 'success', message: `${created.name} foi criada e já está ativa.` })
      await refreshModalities()
    } catch (error) {
      setFeedback({ status: 'error', message: getModalityErrorMessage(error, 'criar') })
    }
  }

  function openEdit(modality: AdminModality) {
    setEditing(modality)
    setEditName(modality.name)
    setEditError(undefined)
    setFeedback(undefined)
  }

  async function saveEdit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!editing) return
    const validationError = validateName(editName)
    setEditError(validationError ?? undefined)
    if (validationError) return
    try {
      const updated = await editMutation.mutateAsync({
        id: editing.id,
        name: normalizeName(editName),
      })
      setEditing(undefined)
      setFeedback({ status: 'success', message: `${updated.name} foi atualizada com sucesso.` })
      await refreshModalities()
    } catch (error) {
      setEditing(undefined)
      setFeedback({ status: 'error', message: getModalityErrorMessage(error, 'editar') })
    }
  }

  async function confirmActivation() {
    if (!pendingActivation) return
    const nextActive = !pendingActivation.active
    setFeedback(undefined)
    try {
      const updated = await activationMutation.mutateAsync({
        id: pendingActivation.id,
        active: nextActive,
      })
      setPendingActivation(undefined)
      setFeedback({
        status: 'success',
        message: `${updated.name} foi ${updated.active ? 'ativada' : 'desativada'}.`,
      })
      await refreshModalities()
    } catch (error) {
      setPendingActivation(undefined)
      setFeedback({ status: 'error', message: getModalityErrorMessage(error, 'alterar') })
    }
  }

  return (
    <div className="mx-auto max-w-[1100px] px-6 py-10 lg:px-10 lg:py-14">
      <div className="flex flex-col gap-5 border-b border-[#292929] pb-8 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="text-xs font-bold tracking-[.18em] text-[#c7ff3d]">CATÁLOGO ADMINISTRATIVO</p>
          <h1 className="mt-3 font-['Manrope'] text-4xl font-extrabold tracking-[-.045em] md:text-5xl">
            Modalidades
          </h1>
          <p className="mt-3 max-w-2xl text-[#aaaaaa]">
            Mantenha as especialidades disponíveis no cadastro e na descoberta de personais.
          </p>
        </div>
        {modalitiesQuery.isSuccess && (
          <div className="flex gap-2 text-center text-xs font-bold">
            <div className="rounded-xl border border-[#c7ff3d]/30 bg-[#c7ff3d]/8 px-4 py-3 text-[#dfff8d]">
              <strong className="block font-['Manrope'] text-xl">{activeCount}</strong> ATIVAS
            </div>
            <div className="rounded-xl border border-[#333] bg-[#111] px-4 py-3 text-[#888]">
              <strong className="block font-['Manrope'] text-xl text-[#aaa]">{modalities.length - activeCount}</strong> INATIVAS
            </div>
          </div>
        )}
      </div>

      {feedback && (
        <div
          className={`mt-8 flex items-start justify-between gap-4 rounded-2xl border px-5 py-4 text-sm ${
            feedback.status === 'success'
              ? 'border-[#c7ff3d]/40 bg-[#c7ff3d]/10'
              : 'border-[#ff6b6b]/40 bg-[#ff6b6b]/10'
          }`}
          role={feedback.status === 'success' ? 'status' : 'alert'}>
          <div>
            <p className={feedback.status === 'success' ? 'font-bold text-[#e8ffad]' : 'font-bold text-[#ffb0b0]'}>
              {feedback.status === 'success' ? 'Operação concluída' : 'A operação falhou'}
            </p>
            <p className="mt-1">{feedback.message}</p>
          </div>
          <button className="rounded-lg px-2 py-1 text-sm font-bold hover:bg-white/10" onClick={() => setFeedback(undefined)} type="button">Fechar</button>
        </div>
      )}

      <section className="mt-8 rounded-[26px] border border-[#7657ff]/30 bg-[#111] p-6 md:p-7">
        <p className="text-[11px] font-bold tracking-[.15em] text-[#7657ff]">NOVA MODALIDADE</p>
        <form className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-start" onSubmit={(event) => void create(event)}>
          <div className="min-w-0 flex-1">
            <label className="sr-only" htmlFor="new-modality-name">Nome da modalidade</label>
            <input
              className="h-12 w-full rounded-xl border border-[#333] bg-[#171717] px-4 text-sm outline-none transition placeholder:text-[#666] focus:border-[#7657ff] disabled:opacity-50"
              disabled={createMutation.isPending}
              id="new-modality-name"
              maxLength={80}
              onChange={(event) => {
                setNewName(event.target.value)
                setCreateError(undefined)
              }}
              placeholder="Ex.: Pilates Clínico"
              value={newName}
            />
            {createError && <p className="mt-1.5 text-xs text-[#ff8a8a]">{createError}</p>}
          </div>
          <button
            className="h-12 rounded-xl bg-[#c7ff3d] px-5 text-sm font-extrabold text-[#080808] transition hover:bg-[#d6ff70] disabled:cursor-wait disabled:opacity-50"
            disabled={createMutation.isPending}
            type="submit">
            {createMutation.isPending ? 'Criando…' : 'Criar modalidade'}
          </button>
        </form>
      </section>

      <section aria-label="Lista de modalidades" className="mt-6">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h2 className="font-['Manrope'] text-xl font-extrabold">Catálogo completo</h2>
          <div className="flex rounded-xl border border-[#333] bg-[#111] p-1" role="group" aria-label="Filtrar modalidades">
            {[
              ['ALL', 'Todas'],
              ['ACTIVE', 'Ativas'],
              ['INACTIVE', 'Inativas'],
            ].map(([value, label]) => (
              <button
                aria-pressed={statusFilter === value}
                className={`rounded-lg px-3 py-2 text-xs font-bold transition ${statusFilter === value ? 'bg-[#f6f4ee] text-[#080808]' : 'text-[#888] hover:text-[#f6f4ee]'}`}
                key={value}
                onClick={() => setStatusFilter(value as ModalityStatusFilter)}
                type="button">
                {label}
              </button>
            ))}
          </div>
        </div>

        {modalitiesQuery.isPending && (
          <div aria-live="polite" className="grid gap-3" role="status">
            <span className="sr-only">Carregando modalidades</span>
            {[0, 1, 2].map((item) => <div className="h-24 animate-pulse rounded-[22px] border border-[#292929] bg-[#111]" key={item} />)}
          </div>
        )}
        {modalitiesQuery.isError && (
          <div className="rounded-[24px] border border-[#ff6b6b]/40 bg-[#ff6b6b]/5 p-8 text-center" role="alert">
            <h3 className="font-['Manrope'] text-xl font-extrabold">Não foi possível carregar as modalidades</h3>
            <p className="mt-2 text-sm text-[#aaa]">{getModalityErrorMessage(modalitiesQuery.error, 'listar')}</p>
            <button className="mt-5 rounded-full bg-[#f6f4ee] px-5 py-2.5 text-sm font-bold text-[#080808]" onClick={() => void modalitiesQuery.refetch()} type="button">Tentar novamente</button>
          </div>
        )}
        {modalitiesQuery.isSuccess && filteredModalities.length === 0 && (
          <div className="rounded-[24px] border border-[#292929] bg-[#111] p-8 text-center" role="status">
            <h3 className="font-['Manrope'] text-xl font-extrabold">Nenhuma modalidade neste filtro</h3>
            <p className="mt-2 text-sm text-[#888]">Altere o filtro ou crie uma nova modalidade.</p>
          </div>
        )}
        {modalitiesQuery.isSuccess && filteredModalities.length > 0 && (
          <div className="grid gap-3">
            {filteredModalities.map((modality) => (
              <article className="flex flex-col gap-4 rounded-[22px] border border-[#292929] bg-[#111] p-5 sm:flex-row sm:items-center sm:justify-between" key={modality.id}>
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className={`rounded-full border px-3 py-1 text-[11px] font-bold ${modality.active ? 'border-[#c7ff3d]/35 bg-[#c7ff3d]/10 text-[#dfff8d]' : 'border-[#555] bg-[#555]/10 text-[#999]'}`}>
                      {modality.active ? 'ATIVA' : 'INATIVA'}
                    </span>
                    <span className="text-xs text-[#666]">/{modality.slug}</span>
                  </div>
                  <h3 className="mt-2 truncate font-['Manrope'] text-xl font-extrabold">{modality.name}</h3>
                </div>
                <div className="flex gap-2">
                  <button className="rounded-full border border-[#444] px-4 py-2.5 text-sm font-bold transition hover:border-[#7657ff] hover:text-[#b5a9ff]" onClick={() => openEdit(modality)} type="button">Editar</button>
                  <button className={`rounded-full px-4 py-2.5 text-sm font-bold transition ${modality.active ? 'border border-[#ff6b6b]/50 text-[#ff8a8a] hover:bg-[#ff6b6b]/10' : 'bg-[#c7ff3d] text-[#080808] hover:bg-[#d6ff70]'}`} onClick={() => setPendingActivation(modality)} type="button">
                    {modality.active ? 'Desativar' : 'Ativar'}
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      {editing && (
        <div aria-labelledby="edit-modality-title" aria-modal="true" className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-5 backdrop-blur-sm" role="dialog">
          <form className="w-full max-w-lg rounded-[28px] border border-[#333] bg-[#111] p-7 shadow-2xl" onSubmit={(event) => void saveEdit(event)}>
            <p className="text-xs font-bold tracking-[.14em] text-[#7657ff]">EDITAR MODALIDADE</p>
            <h2 className="mt-3 font-['Manrope'] text-2xl font-extrabold" id="edit-modality-title">Alterar nome</h2>
            <p className="mt-2 text-sm leading-6 text-[#888]">O identificador público será regenerado automaticamente a partir do novo nome.</p>
            <label className="mt-6 block text-sm font-bold" htmlFor="edit-modality-name">Nome</label>
            <input autoFocus className="mt-2 h-12 w-full rounded-xl border border-[#333] bg-[#080808] px-4 text-sm outline-none focus:border-[#7657ff]" disabled={editMutation.isPending} id="edit-modality-name" maxLength={80} onChange={(event) => { setEditName(event.target.value); setEditError(undefined) }} value={editName} />
            {editError && <p className="mt-1.5 text-xs text-[#ff8a8a]">{editError}</p>}
            <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <button className="rounded-xl border border-[#444] px-5 py-3 text-sm font-bold" disabled={editMutation.isPending} onClick={() => setEditing(undefined)} type="button">Cancelar</button>
              <button className="rounded-xl bg-[#c7ff3d] px-5 py-3 text-sm font-extrabold text-[#080808] disabled:opacity-50" disabled={editMutation.isPending} type="submit">{editMutation.isPending ? 'Salvando…' : 'Salvar nome'}</button>
            </div>
          </form>
        </div>
      )}

      {pendingActivation && (
        <div aria-labelledby="activation-modality-title" aria-modal="true" className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-5 backdrop-blur-sm" role="dialog">
          <div className="w-full max-w-lg rounded-[28px] border border-[#333] bg-[#111] p-7 shadow-2xl">
            <p className={`text-xs font-bold tracking-[.14em] ${pendingActivation.active ? 'text-[#ff8a8a]' : 'text-[#c7ff3d]'}`}>CONFIRMAR AÇÃO</p>
            <h2 className="mt-3 font-['Manrope'] text-2xl font-extrabold" id="activation-modality-title">
              {pendingActivation.active ? `Desativar ${pendingActivation.name}?` : `Ativar ${pendingActivation.name}?`}
            </h2>
            <p className="mt-3 text-sm leading-6 text-[#aaa]">
              {pendingActivation.active
                ? 'Ela deixará de aparecer como opção em novos cadastros e edições. Perfis existentes não serão apagados.'
                : 'Ela voltará a aparecer como opção nos formulários de perfis profissionais.'}
            </p>
            <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <button className="rounded-xl border border-[#444] px-5 py-3 text-sm font-bold" disabled={activationMutation.isPending} onClick={() => setPendingActivation(undefined)} type="button">Cancelar</button>
              <button className={`rounded-xl px-5 py-3 text-sm font-extrabold disabled:opacity-50 ${pendingActivation.active ? 'bg-[#ff6b6b] text-[#080808]' : 'bg-[#c7ff3d] text-[#080808]'}`} disabled={activationMutation.isPending} onClick={() => void confirmActivation()} type="button">
                {activationMutation.isPending ? 'Salvando…' : pendingActivation.active ? 'Confirmar desativação' : 'Confirmar ativação'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
