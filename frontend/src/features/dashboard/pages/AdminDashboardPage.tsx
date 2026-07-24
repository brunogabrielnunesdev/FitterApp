import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'

import { Logo } from '../../../common/components/Logo'
import { api } from '../../../common/services/api'
import { useAuth } from '../../auth/context/useAuth'

const metrics = [
  { label: 'Usuários ativos', value: '—', accent: 'text-[#c7ff3d]' },
  { label: 'Personais publicados', value: '—', accent: 'text-[#f6f4ee]' },
  { label: 'Regiões alcançadas', value: '—', accent: 'text-[#7657ff]' },
]

export function AdminDashboardPage() {
  const { email, logout } = useAuth()
  const navigate = useNavigate()
  const healthQuery = useQuery({
    queryKey: ['api-health'],
    queryFn: async () => (await api.get<{ status: string }>('/actuator/health')).data,
    refetchInterval: 30_000,
  })

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <main className="min-h-screen bg-[#080808] text-[#f6f4ee]">
      <header className="border-b border-[#292929] bg-[#080808]/95 px-6 backdrop-blur lg:px-10">
        <div className="mx-auto flex h-20 max-w-[1440px] items-center justify-between">
          <Logo compact />
          <div className="flex items-center gap-5">
            <div className="hidden text-right sm:block">
              <p className="text-xs font-bold tracking-[.12em] text-[#777]">ADMIN</p>
              <p className="mt-1 text-sm text-[#aaaaaa]">{email}</p>
            </div>
            <button
              className="rounded-full border border-[#292929] px-5 py-2.5 text-sm font-bold transition hover:border-[#c7ff3d] hover:text-[#c7ff3d]"
              onClick={handleLogout}
              type="button">
              Sair
            </button>
          </div>
        </div>
      </header>

      <div className="mx-auto max-w-[1440px] px-6 py-12 lg:px-10 lg:py-16">
        <div className="flex flex-col gap-6 border-b border-[#292929] pb-10 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="mb-3 text-xs font-bold tracking-[.18em] text-[#c7ff3d]">VISÃO GERAL</p>
            <h1 className="font-['Manrope'] text-4xl font-extrabold tracking-[-.045em] md:text-5xl">
              Operação FitterApp
            </h1>
            <p className="mt-4 max-w-xl text-[#aaaaaa]">
              Dashboard inicial da validação regional. As métricas serão conectadas conforme os
              módulos administrativos forem implementados.
            </p>
          </div>
          <div className="flex items-center gap-2 text-xs font-bold tracking-[.12em] text-[#aaaaaa]">
            <span
              className={`h-2.5 w-2.5 rounded-full ${
                healthQuery.isSuccess ? 'bg-[#c7ff3d]' : 'bg-[#ff6b6b]'
              }`}
            />
            {healthQuery.isSuccess ? 'API CONECTADA' : 'API INDISPONÍVEL'}
          </div>
        </div>

        <section className="grid gap-4 py-8 md:grid-cols-3">
          {metrics.map((metric) => (
            <article
              className="rounded-[26px] border border-[#292929] bg-[#111] p-6 transition hover:-translate-y-1 hover:border-[#3a3a3a]"
              key={metric.label}>
              <p className="text-xs font-bold tracking-[.13em] text-[#777] uppercase">
                {metric.label}
              </p>
              <p className={`mt-8 font-['Manrope'] text-5xl font-extrabold ${metric.accent}`}>
                {metric.value}
              </p>
            </article>
          ))}
        </section>

        <section className="grid gap-4 lg:grid-cols-[1.25fr_.75fr]">
          <article className="min-h-72 rounded-[30px] border border-[#292929] bg-[#111] p-7">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-bold tracking-[.13em] text-[#7657ff]">PRÓXIMO MÓDULO</p>
                <h2 className="mt-3 font-['Manrope'] text-2xl font-extrabold">Gestão de usuários</h2>
              </div>
              <span className="rounded-full border border-[#292929] px-3 py-1.5 text-xs text-[#777]">
                EM BREVE
              </span>
            </div>
            <div className="mt-12 space-y-3">
              {[72, 54, 83].map((width) => (
                <div className="h-3 rounded-full bg-[#1c1c1c]" key={width} style={{ width: `${width}%` }} />
              ))}
            </div>
          </article>

          <article className="rounded-[30px] border border-[#c7ff3d]/25 bg-[#c7ff3d] p-7 text-[#080808]">
            <p className="text-xs font-extrabold tracking-[.13em]">STATUS DO MVP</p>
            <p className="mt-8 font-['Manrope'] text-6xl font-extrabold tracking-[-.06em]">01</p>
            <p className="mt-3 text-lg font-bold">Autenticação integrada</p>
            <p className="mt-2 text-sm leading-6 opacity-70">
              API, mobile e painel administrativo conectados ao mesmo fluxo.
            </p>
          </article>
        </section>
      </div>
    </main>
  )
}
