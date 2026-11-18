import { useQuery } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'

import { api } from '../../../common/services/api'
import { getAdminDashboard, getDailyMetrics } from '../services/dashboardService'
import type { DailyMetric, DailyMetricKey, DashboardRange } from '../types/dashboard'
import { getDashboardErrorMessage } from '../utils/getDashboardErrorMessage'

type PeriodPreset = 7 | 30 | 90 | 'custom'

const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone || 'America/Sao_Paulo'
const numberFormatter = new Intl.NumberFormat('pt-BR')

function formatCount(value: number | null) {
  return value === null ? "Indisponível" : numberFormatter.format(value)
}

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

const metricOptions: { key: DailyMetricKey; label: string; needsTracking: boolean }[] = [
  { key: 'searches', label: 'Pesquisas', needsTracking: true },
  { key: 'profileViews', label: 'Visualizações de perfil', needsTracking: true },
  { key: 'whatsappContacts', label: 'Contatos no WhatsApp', needsTracking: false },
  { key: 'returns', label: 'Retornos ao aplicativo', needsTracking: true },
  { key: 'accountsRegistered', label: 'Contas registradas', needsTracking: false },
  { key: 'accountsCompleted', label: 'Contas confirmadas', needsTracking: false },
  { key: 'profilesStarted', label: 'Perfis iniciados', needsTracking: false },
  { key: 'profilesSubmitted', label: 'Perfis enviados', needsTracking: true },
  { key: 'profilesApproved', label: 'Perfis aprovados', needsTracking: true },
  { key: 'profilesRejected', label: 'Perfis reprovados', needsTracking: true },
  { key: 'modality', label: 'Filtro de modalidade', needsTracking: true },
  { key: 'neighborhood', label: 'Filtro de bairro', needsTracking: true },
  { key: 'serviceMode', label: 'Filtro de atendimento', needsTracking: true },
]

function LineChart({ days, metric, trackingStartedAt }: { days: DailyMetric[]; metric: typeof metricOptions[number]; trackingStartedAt: string }) {
  const [selected, setSelected] = useState<number | null>(null)
  const started = new Intl.DateTimeFormat('en-CA', { timeZone: timezone, year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(trackingStartedAt))
  const points = days.map((day) => ({ date: day.date, value: metric.needsTracking && day.date < started ? null : (day.counts[metric.key] ?? 0) }))
  const max = Math.max(1, ...points.map((point) => point.value ?? 0))
  const x = (index: number) => points.length === 1 ? 310 : 42 + index * 542 / (points.length - 1)
  const y = (value: number) => 168 - value / max * 140
  const segments: { x: number; y: number }[][] = []
  points.forEach((point, index) => {
    if (point.value === null) return
    if (index === 0 || points[index - 1].value === null) segments.push([])
    segments[segments.length - 1].push({ x: x(index), y: y(point.value) })
  })
  const selectedIndex = Math.min(selected ?? Math.max(0, points.length - 1), Math.max(0, points.length - 1))
  const active = points[selectedIndex]
  const formatDay = (value: string) => new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short', timeZone: 'UTC' }).format(new Date(`${value}T12:00:00Z`))
  return (
    <div>
      <div className="mb-3 flex items-baseline justify-between gap-4 text-sm">
        <span className="text-[#999]">{active ? formatPeriodDate(active.date) : 'Sem dados'}</span>
        <span className="font-bold tabular-nums text-[#f6f4ee]">{active?.value == null ? 'Sem histórico' : numberFormatter.format(active.value)}</span>
      </div>
      <svg className="block h-48 w-full overflow-visible sm:h-56" viewBox="0 0 600 180" preserveAspectRatio="none" role="img" aria-label={`Evolução diária: ${metric.label}`}>
        {[0, 0.5, 1].map((level) => <line key={level} x1="42" x2="584" y1={168 - 140 * level} y2={168 - 140 * level} stroke="#303030" strokeWidth="1" vectorEffect="non-scaling-stroke" />)}
        <text x="2" y="32" fill="#999" fontSize="10">{numberFormatter.format(max)}</text>
        <text x="2" y="171" fill="#999" fontSize="10">0</text>
        {segments.map((segment, index) => <polyline key={index} points={segment.map((point) => `${point.x},${point.y}`).join(' ')} fill="none" stroke="#c7ff3d" strokeWidth="2.5" vectorEffect="non-scaling-stroke" />)}
        {active && active.value !== null && <circle cx={x(selectedIndex)} cy={y(active.value)} r="4" fill="#c7ff3d" />}
      </svg>
      <div className="mt-2 flex justify-between text-xs text-[#999]" aria-hidden="true"><span>{points[0] ? formatDay(points[0].date) : ''}</span><span>{points.length > 1 ? formatDay(points[points.length - 1].date) : ''}</span></div>
      {points.length > 1 && <label className="mt-5 block text-xs text-[#aaa]">Escolha um dia para consultar o valor
        <input className="mt-2 block w-full accent-[#c7ff3d]" type="range" min="0" max={points.length - 1} value={selectedIndex} onChange={(event) => setSelected(Number(event.target.value))} aria-valuetext={`${formatPeriodDate(active.date)}: ${active.value == null ? 'sem histórico' : active.value}`} />
      </label>}
      {metric.needsTracking && points.some((point) => point.value === null) && <p className="mt-3 text-xs leading-5 text-[#999]">Esta métrica começou a ser registrada em {formatPeriodDate(started)}. Dias anteriores ficam sem histórico.</p>}
    </div>
  )
}

export function AdminDashboardPage() {
  const initialRange = presetRange(30)
  const [range, setRange] = useState<DashboardRange>(initialRange)
  const [draftRange, setDraftRange] = useState<DashboardRange>(initialRange)
  const [activePreset, setActivePreset] = useState<PeriodPreset>(30)
  const [periodError, setPeriodError] = useState<string>()
  const [metricKey, setMetricKey] = useState<DailyMetricKey>('accountsRegistered')
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

  const dailyQuery = useQuery({
    queryKey: ['admin-dashboard-daily', range.from, range.to, timezone],
    queryFn: () => getDailyMetrics(range, timezone),
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
    if ((Date.parse(`${draftRange.to}T00:00:00Z`) - Date.parse(`${draftRange.from}T00:00:00Z`)) / 86400000 > 365) {
      setPeriodError('Selecione um período de até 366 dias.')
      return
    }
    setPeriodError(undefined)
    setActivePreset('custom')
    setRange(draftRange)
  }

  const data = metricsQuery.data
  const funnelMetrics = data
    ? [
        { label: 'Contas registradas', value: data.funnel.accountsRegistered, accent: 'text-[#c7ff3d]' },
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
        { label: 'Retornos ao app', description: 'Voltas ao aplicativo', metrics: data.returns },
      ]
    : []
  const hasNoData =
    data !== undefined &&
    [...Object.values(data.funnel), data.searches.raw, data.profileViews.raw, data.whatsappContacts.raw, data.returns.raw]
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

            <section aria-labelledby="daily-heading" className="rounded-[20px] bg-[#111] p-5 md:p-7">
              <div className="mb-7 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
                <div><h2 id="daily-heading" className="font-['Manrope'] text-xl font-extrabold">Evolução diária</h2><p className="mt-2 text-sm leading-6 text-[#999]">Acompanhe os dados ao longo do período selecionado.</p></div>
                <label className="text-xs text-[#aaa]">Métrica
                  <select className="mt-2 block w-full rounded-xl border border-[#444] bg-[#191919] px-3 py-2 text-sm text-[#f6f4ee] sm:w-56" value={metricKey} onChange={(event) => setMetricKey(event.target.value as DailyMetricKey)}>
                    {metricOptions.map((option) => <option key={option.key} value={option.key}>{option.label}</option>)}
                  </select>
                </label>
              </div>
              {dailyQuery.isPending && <p className="py-16 text-sm text-[#999]" role="status">Carregando gráfico...</p>}
              {dailyQuery.isError && <div className="py-12 text-sm text-[#ff8a8a]" role="alert">Não foi possível carregar o gráfico. <button type="button" className="underline" onClick={() => void dailyQuery.refetch()}>Tentar novamente</button></div>}
              {dailyQuery.data && <LineChart key={`${metricKey}-${range.from}-${range.to}`} days={dailyQuery.data} metric={metricOptions.find((option) => option.key === metricKey)!} trackingStartedAt={data.trackingStartedAt} />}
            </section>
            <section aria-labelledby="summary-heading" className="mt-4 rounded-[20px] bg-[#111] p-5 md:p-7">
              <h2 id="summary-heading" className="font-['Manrope'] text-xl font-extrabold">Totais do período</h2>
              <p className="mt-2 text-sm text-[#999]">Os valores de perfis são distintos dentro do período; eventos incluem repetições válidas.</p>
              <dl className="mt-5 grid gap-x-8 gap-y-4 sm:grid-cols-2 xl:grid-cols-3">
                {[...funnelMetrics, ...eventMetrics.flatMap((event) => [{ label: event.label, value: event.metrics.raw }, { label: `${event.label} · únicos`, value: event.metrics.unique }]), { label: 'Filtro · modalidade', value: data.filters.modality }, { label: 'Filtro · bairro', value: data.filters.neighborhood }, { label: 'Filtro · atendimento', value: data.filters.serviceMode }].map((item) => <div key={item.label} className="flex items-center justify-between gap-4 border-b border-[#292929] pb-3 text-sm"><dt className="text-[#aaa]">{item.label}</dt><dd className="font-bold tabular-nums text-[#f6f4ee]">{formatCount(item.value)}</dd></div>)}
              </dl>
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
                <p className="mt-4 text-sm leading-6 text-[#aaa]">Contagens únicas consideram visitantes identificados ou contas autenticadas; eventos antigos sem identificação entram apenas nos totais. As ações de revisão são registradas a partir de {new Date(data.trackingStartedAt).toLocaleDateString('pt-BR')}.</p>
              </article>
            </section>
          </>
        )}
      </div>
    </div>
  )
}
