import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'

import { Logo } from '../../../common/components/Logo'
import { useAuth } from '../context/useAuth'
import { login } from '../services/authService'
import { getLoginErrorMessage } from '../utils/getLoginErrorMessage'
import { loginSchema, type LoginForm } from '../validation/loginSchema'

export function AdminLoginPage() {
  const [passwordVisible, setPasswordVisible] = useState(false)
  const [accessDenied, setAccessDenied] = useState(false)
  const { isAdmin, startSession } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })
  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: (session) => {
      if (!startSession(session)) {
        setAccessDenied(true)
        return
      }
      const destination = (location.state as { from?: string } | null)?.from ?? '/admin'
      navigate(destination, { replace: true })
    },
  })

  if (isAdmin) {
    return <Navigate replace to="/admin" />
  }

  return (
    <main className="relative min-h-screen overflow-hidden bg-[#080808] text-[#f6f4ee]">
      <div className="pointer-events-none absolute -left-32 bottom-[-15rem] h-[32rem] w-[32rem] rounded-full bg-[#7657ff]/10 blur-3xl" />
      <div className="pointer-events-none absolute -right-40 top-[-18rem] h-[36rem] w-[36rem] rounded-full bg-[#c7ff3d]/10 blur-3xl" />
      <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(rgba(255,255,255,.025)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,.025)_1px,transparent_1px)] bg-[size:72px_72px]" />

      <div className="relative mx-auto grid min-h-screen max-w-[1440px] lg:grid-cols-[1.08fr_.92fr]">
        <section className="flex min-h-[46vh] flex-col justify-between border-b border-[#292929] px-6 py-7 sm:px-10 lg:min-h-screen lg:border-r lg:border-b-0 lg:px-16 lg:py-12">
          <Logo />

          <div className="max-w-2xl py-14 lg:py-20">
            <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-[#c7ff3d]/25 bg-[#c7ff3d]/8 px-4 py-2 text-[11px] font-bold tracking-[.18em] text-[#c7ff3d]">
              <span className="h-2 w-2 rounded-full bg-[#c7ff3d] shadow-[0_0_16px_#c7ff3d]" />
              OPERAÇÃO FITTERAPP
            </div>
            <h1 className="max-w-[680px] font-['Manrope'] text-5xl leading-[.98] font-extrabold tracking-[-.055em] sm:text-6xl xl:text-[5.25rem]">
              Gestão direta.
              <span className="block text-[#c7ff3d]">Decisões melhores.</span>
            </h1>
            <p className="mt-7 max-w-xl text-base leading-7 text-[#aaaaaa] sm:text-lg">
              Acompanhe a operação regional, usuários e evolução do marketplace em um só lugar.
            </p>
          </div>

          <div className="flex items-center gap-4 text-xs font-semibold tracking-[.12em] text-[#777]">
            <span>ADMINISTRAÇÃO</span>
            <span className="h-px w-12 bg-[#292929]" />
            <span>ACESSO RESTRITO</span>
          </div>
        </section>

        <section className="flex items-center justify-center px-6 py-12 sm:px-10 lg:px-16">
          <div className="w-full max-w-[460px]">
            <div className="mb-9">
              <p className="mb-3 text-xs font-bold tracking-[.18em] text-[#7657ff]">PAINEL ADMIN</p>
              <h2 className="font-['Manrope'] text-3xl font-extrabold tracking-[-.035em] sm:text-4xl">
                Acesse sua conta
              </h2>
              <p className="mt-3 text-sm leading-6 text-[#aaaaaa]">
                Entre com uma conta que possua permissão de administrador.
              </p>
            </div>

            <form
              className="rounded-[30px] border border-[#292929] bg-[#111]/90 p-5 shadow-2xl shadow-black/35 backdrop-blur sm:p-7"
              onSubmit={handleSubmit((form) => {
                setAccessDenied(false)
                loginMutation.mutate(form)
              })}>
              <div className="space-y-5">
                <label className="block">
                  <span className="mb-2 block text-[11px] font-bold tracking-[.15em] text-[#aaaaaa]">
                    E-MAIL
                  </span>
                  <input
                    autoComplete="email"
                    className="h-14 w-full rounded-2xl border border-[#292929] bg-[#080808] px-4 text-[#f6f4ee] outline-none transition focus:border-[#c7ff3d] focus:ring-3 focus:ring-[#c7ff3d]/10"
                    placeholder="admin@fitterapp.com"
                    type="email"
                    {...register('email')}
                  />
                  {errors.email && (
                    <span className="mt-2 block text-xs text-[#ff6b6b]">{errors.email.message}</span>
                  )}
                </label>

                <label className="block">
                  <span className="mb-2 flex items-center justify-between text-[11px] font-bold tracking-[.15em] text-[#aaaaaa]">
                    SENHA
                    <button
                      className="text-[#c7ff3d] transition hover:text-white"
                      onClick={() => setPasswordVisible((visible) => !visible)}
                      type="button">
                      {passwordVisible ? 'OCULTAR' : 'MOSTRAR'}
                    </button>
                  </span>
                  <input
                    autoComplete="current-password"
                    className="h-14 w-full rounded-2xl border border-[#292929] bg-[#080808] px-4 text-[#f6f4ee] outline-none transition focus:border-[#c7ff3d] focus:ring-3 focus:ring-[#c7ff3d]/10"
                    placeholder="Sua senha"
                    type={passwordVisible ? 'text' : 'password'}
                    {...register('password')}
                  />
                  {errors.password && (
                    <span className="mt-2 block text-xs text-[#ff6b6b]">
                      {errors.password.message}
                    </span>
                  )}
                </label>

                {(loginMutation.isError || accessDenied) && (
                  <div
                    className="rounded-2xl border border-[#ff6b6b]/30 bg-[#ff6b6b]/8 px-4 py-3 text-sm text-[#ff8b8b]"
                    role="alert">
                    {accessDenied
                      ? 'Esta conta não possui acesso administrativo.'
                      : getLoginErrorMessage(loginMutation.error)}
                  </div>
                )}

                <button
                  className="flex h-14 w-full items-center justify-center rounded-full bg-[#c7ff3d] font-bold text-[#080808] transition hover:-translate-y-0.5 hover:bg-white disabled:cursor-wait disabled:opacity-60"
                  disabled={loginMutation.isPending}
                  type="submit">
                  {loginMutation.isPending ? 'Entrando...' : 'Entrar no painel'}
                </button>
              </div>
            </form>

            <p className="mt-6 text-center text-xs leading-5 text-[#777]">
              Ambiente exclusivo para a administração do FitterApp.
            </p>
          </div>
        </section>
      </div>
    </main>
  )
}
