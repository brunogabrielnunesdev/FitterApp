import { useQuery } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'

import { api } from '../../../common/services/api'
import { getAdminDashboard } from '../services/dashboardService'
import type { DashboardRange } from '../types/dashboard'
import { getDashboardErrorMessage } from '../utils/getDashboardErrorMessage'

type PeriodPreset = 7 | 30 | 90 | 'custom'

const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone || 'America/Sao_Paulo'
const numberFormatter = new Intl.NumberFormat('pt-BR')

function toDateInput(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function presetRange(days: number): DashboardRange {
  const to = new Date()
  const from = new Date(to)
  from.setDate(from.getDate() - (days - 1))
  return { from: toDateInput(from), to: toDateInput(to) }
}

function formatPeriodDate(value: string) {
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium', timeZone: 'UTC' }).format(
    new Date(`${value}T12:00:00Z`),
  )
}

function MetricSkeleton() {
  return (
    <div aria-live="polite" className="grid gap-4 md:grid-cols-3" role="status">
      <span className="sr-only">Carregando métricas do dashboard</span>
      {[0, 1, 2, 3, 4, 5].map((item) => (
        <div className="h-40 animate-pulse rounded-[26px] border border-[#292929] bg-[#111]" key={item} />
      ))}
    </div>
  )
}

export function AdminDashboardPage() {
  const initialRange = presetRange(30)
  const [range, setRange] = useState<DashboardRange>(initialRange)
  const [draftRange, setDraftRange] = useState<DashboardRange>(initialRange)
  const [activePreset, setActivePreset] = useState<PeriodPreset>(30)
  const [periodError, setPeriodError] = useState<string>()
  const healthQuery = useQuery({
    queryKey: ['api-health'],
    queryFn: async () => (await api.get<{ status: string }>('/actuator/health')).data,
    refetchInterval: 30_000,
  })
  const metricsQuery = useQuery({
    queryKey: ['admin-dashboard', range.from, range.to, timezone],
    queryFn: () => getAdminDashboard(range, timezone),
    retry: false,
    staleTime: 30_000,
  })

  function selectPreset(days: 7 | 30 | 90) {
    const nextRange = presetRange(days)
    setRange(nextRange)
    setDraftRange(nextRange)
    setActivePreset(days)
    setPeriodError(undefined)
  }

  function applyCustomRange(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!draftRange.from || !draftRange.to) {
      setPeriodError('Informe as duas datas do período.')
      return
    }
    if (draftRange.from > draftRange.to) {
      setPeriodError('A data inicial não pode ser posterior à data final.')
      return
    }
    setPeriodError(undefined)
    setActivePreset('custom')
    setRange(draftRange)
  }

  const data = metricsQuery.data
  const funnelMetrics = data
    ? [
        { label: 'Contas concluídas', value: data.funnel.accountsCompleted, accent: 'text-[#c7ff3d]' },
        { label: 'Perfis iniciados', value: data.funnel.profilesStarted, accent: 'text-[#f6f4ee]' },
        { label: 'Perfis enviados', value: data.funnel.profilesSubmitted, accent: 'text-[#a999ff]' },
        { label: 'Perfis aprovados', value: data.funnel.profilesApproved, accent: 'text-[#8ee35a]' },
        { label: 'Perfis reprovados', value: data.funnel.profilesRejected, accent: 'text-[#ff9f6e]' },
      ]
    : []
  const eventMetrics = data
    ? [
        { label: 'Pesquisas', description: 'Consultas ao catálogo', metrics: data.searches },
        { label: 'Perfis visualizados', description: 'Aberturas de detalhes', metrics: data.profileViews },
        { label: 'Contatos no WhatsApp', description: 'Intenção de conversão', metrics: data.whatsappContacts },
      ]
    : []
  const hasNoData =
    data !== undefined &&
    [...Object.values(data.funnel), data.searches.raw, data.profileViews.raw, data.whatsappContacts.raw]
      .every((value) => value === 0)

  const healthLabel = healthQuery.isPending
    ? 'VERIFICANDO API'
    : healthQuery.isSuccess
      ? 'API CONECTADA'
      : 'API INDISPONÍVEL'
  const healthColor = healthQuery.isPending
    ? 'bg-[#ffcf5a]'
    : healthQuery.isSuccess
      ? 'bg-[#c7ff3d]'
      : 'bg-[#ff6b6b]'

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-10 lg:px-10 lg:py-14">
      <div className="flex flex-col gap-7 border-b border-[#292929] pb-9 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-xs font-bold tracking-[.18em] text-[#c7ff3d]">VISÃO GERAL</p>
          <h1 className="mt-3 font-['Manrope'] text-4xl font-extrabold tracking-[-.045em] md:text-5xl">
            Operação FitterApp
          </h1>
          <p className="mt-4 max-w-xl text-[#aaaaaa]">
            Acompanhe o funil real do piloto, da criação da conta até o contato com o personal.
          </p>
        </div>

        <aside className="w-fit rounded-2xl border border-[#292929] bg-[#111] px-5 py-4" aria-label="Saúde da API">
          <p className="text-[10px] font-bold tracking-[.14em] text-[#666]">INFRAESTRUTURA</p>
          <div className="mt-2 flex items-center gap-2 text-xs font-bold tracking-[.1em] text-[#aaa]">
            <span className={`h-2.5 w-2.5 rounded-full ${healthColor}`} />
            {healthLabel}
          </div>
        </aside>
      </div>

      <section aria-label="Período das métricas" className="mt-8 rounded-[26px] border border-[#292929] bg-[#111] p-5 md:p-6">
        <div className="flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
          <div>
            <p className="text-xs font-bold tracking-[.12em] text-[#7657ff]">PERÍODO DE APURAÇÃO</p>
            <div className="mt-3 flex flex-wrap gap-2" role="group" aria-label="Atalhos de período">
              {[7, 30, 90].map((days) => (
                <button
                  aria-pressed={activePreset === days}
                  className={`rounded-full px-4 py-2.5 text-sm font-bold transition ${
                    activePreset === days
                      ? 'bg-[#f6f4ee] text-[#080808]'
                      : 'border border-[#333] text-[#888] hover:border-[#666] hover:text-[#f6f4ee]'
                  }`}
                  key={days}
                  onClick={() => selectPreset(days as 7 | 30 | 90)}
                  type="button">
                  {days} dias
                </button>
              ))}
            </div>
          </div>

          <form className="flex flex-col gap-3 sm:flex-row sm:items-end" onSubmit={applyCustomRange}>
            <div>
              <label className="text-xs font-bold text-[#777]" htmlFor="metrics-from">De</label>
              <input
                className="mt-1.5 h-11 w-full rounded-xl border border-[#333] bg-[#171717] px-3 text-sm outline-none focus:border-[#7657ff] sm:w-40"
                id="metrics-from"
                onChange={(event) => setDraftRange((current) => ({ ...current, from: event.target.value }))}
                type="date"
                value={draftRange.from}
              />
            </div>
            <div>
              <label className="text-xs font-bold text-[#777]" htmlFor="metrics-to">Até</label>
              <input
                className="mt-1.5 h-11 w-full rounded-xl border border-[#333] bg-[#171717] px-3 text-sm outline-none focus:border-[#7657ff] sm:w-40"
                id="metrics-to"
                onChange={(event) => setDraftRange((current) => ({ ...current, to: event.target.value }))}
                type="date"
                value={draftRange.to}
              />
            </div>
            <button className="h-11 rounded-xl bg-[#7657ff] px-5 text-sm font-extrabold transition hover:bg-[#8d73ff]" type="submit">
              Aplicar
            </button>
          </form>
        </div>
        {periodError && <p className="mt-3 text-sm text-[#ff8a8a]" role="alert">{periodError}</p>}
        {data && (
          <p className="mt-4 border-t border-[#292929] pt-4 text-xs text-[#777]">
            Dados de <strong className="text-[#aaa]">{formatPeriodDate(data.period.from)}</strong> até{' '}
            <strong className="text-[#aaa]">{formatPeriodDate(data.period.to)}</strong> · timezone{' '}
            <strong className="text-[#aaa]">{data.period.timezone}</strong>
          </p>
        )}
      </section>

      <div className="mt-8">
        {metricsQuery.isPending && <MetricSkeleton />}
        {metricsQuery.isError && (
          <section className="rounded-[26px] border border-[#ff6b6b]/40 bg-[#ff6b6b]/5 p-8 text-center" role="alert">
            <h2 className="font-['Manrope'] text-xl font-extrabold">Métricas temporariamente indisponíveis</h2>
            <p className="mx-auto mt-2 max-w-xl text-sm leading-6 text-[#c2b8b8]">
              {getDashboardErrorMessage(metricsQuery.error)}
            </p>
            <button
              className="mt-5 rounded-full bg-[#f6f4ee] px-5 py-2.5 text-sm font-bold text-[#080808] disabled:opacity-50"
              disabled={metricsQuery.isFetching}
              onClick={() => void metricsQuery.refetch()}
              type="button">
              {metricsQuery.isFetching ? 'Atualizando…' : 'Tentar novamente'}
            </button>
          </section>
        )}

        {data && (
          <>
            {hasNoData && (
              <div className="mb-6 rounded-2xl border border-[#7657ff]/35 bg-[#7657ff]/8 px-5 py-4" role="status">
                <p className="font-bold text-[#c7c0ff]">Nenhum evento neste período</p>
                <p className="mt-1 text-sm text-[#aaa]">As métricas estão disponíveis, mas ainda não há atividade registrada no intervalo selecionado.</p>
              </div>
            )}

            <section aria-labelledby="funnel-heading">
              <div className="flex items-end justify-between gap-4">
                <div>
                  <p className="text-xs font-bold tracking-[.13em] text-[#777]">FUNIL DO MVP</p>
                  <h2 className="mt-2 font-['Manrope'] text-2xl font-extrabold" id="funnel-heading">Evolução dos perfis</h2>
                </div>
                <span className="text-xs text-[#666]">Números do período</span>
              </div>
              <div className="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-5">
                {funnelMetrics.map((metric, index) => (
                  <article className="relative overflow-hidden rounded-[24px] border border-[#292929] bg-[#111] p-5" key={metric.label}>
                    <span className="text-[10px] font-bold tracking-[.12em] text-[#555]">ETAPA {String(index + 1).padStart(2, '0')}</span>
                    <p className={`mt-7 font-['Manrope'] text-4xl font-extrabold ${metric.accent}`}>
                      {numberFormatter.format(metric.value)}
                    </p>
                    <h3 className="mt-2 text-sm font-bold text-[#aaa]">{metric.label}</h3>
                  </article>
                ))}
              </div>
            </section>

            <section aria-labelledby="engagement-heading" className="mt-8">
              <p className="text-xs font-bold tracking-[.13em] text-[#777]">ENGAJAMENTO</p>
              <h2 className="mt-2 font-['Manrope'] text-2xl font-extrabold" id="engagement-heading">Descoberta e contato</h2>
              <div className="mt-5 grid gap-4 lg:grid-cols-3">
                {eventMetrics.map((event, index) => (
                  <article className={`rounded-[26px] border p-6 ${index === 2 ? 'border-[#c7ff3d]/30 bg-[#c7ff3d] text-[#080808]' : 'border-[#292929] bg-[#111]'}`} key={event.label}>
                    <p className={`text-xs font-bold tracking-[.12em] ${index === 2 ? 'opacity-60' : 'text-[#7657ff]'}`}>{event.label.toUpperCase()}</p>
                    <p className="mt-7 font-['Manrope'] text-5xl font-extrabold tracking-[-.04em]">{numberFormatter.format(event.metrics.unique)}</p>
                    <p className={`mt-2 text-sm ${index === 2 ? 'opacity-70' : 'text-[#aaa]'}`}>{event.description} · únicos</p>
                    <div className={`mt-5 border-t pt-4 text-xs ${index === 2 ? 'border-black/15 opacity-60' : 'border-[#292929] text-[#666]'}`}>
                      {numberFormatter.format(event.metrics.raw)} eventos totais registrados
                    </div>
                  </article>
                ))}
              </div>
            </section>

            <section className="mt-8 grid gap-4 lg:grid-cols-[1.2fr_.8fr]">
              <article className="rounded-[26px] border border-[#7657ff]/30 bg-[#111] p-6">
                <p className="text-xs font-bold tracking-[.13em] text-[#7657ff]">MODERAÇÃO</p>
                <h2 className="mt-3 font-['Manrope'] text-xl font-extrabold">Fila de personais</h2>
                <p className="mt-3 max-w-xl text-sm leading-6 text-[#aaa]">Analise perfis enviados, aprove profissionais ou informe os ajustes necessários.</p>
                <Link className="mt-6 inline-flex rounded-full bg-[#f6f4ee] px-5 py-3 text-sm font-extrabold text-[#080808] transition hover:bg-[#c7ff3d]" to="/admin/personals/pending">
                  Abrir fila de análise
                </Link>
              </article>
              <article className="rounded-[26px] border border-[#292929] bg-[#111] p-6">
                <p className="text-xs font-bold tracking-[.13em] text-[#777]">LEITURA DOS DADOS</p>
                <p className="mt-4 text-sm leading-6 text-[#aaa]">Contagens únicas removem repetições conforme a regra de deduplicação da API. Contagens totais representam todos os eventos aceitos no período.</p>
              </article>
            </section>
          </>
        )}
      </div>
    </div>
  )
}
