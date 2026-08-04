import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'

import { requestPasswordReset, resetPassword } from '../services/authService'

export function PasswordRecoveryPage({ reset = false }: { reset?: boolean }) {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [token, setToken] = useState(params.get('token') ?? '')
  const [password, setPassword] = useState('')
  const mutation = useMutation({
    mutationFn: () => reset ? resetPassword(token.trim(), password) : requestPasswordReset(email.trim()),
    onSuccess: () => { if (reset) setTimeout(() => navigate('/login'), 1200) },
  })

  return <main className="flex min-h-screen items-center justify-center bg-[#080808] px-6 text-[#f6f4ee]"><section className="w-full max-w-md rounded-[30px] border border-[#292929] bg-[#111] p-7"><p className="text-xs font-bold tracking-[.18em] text-[#c7ff3d]">RECUPERAR ACESSO</p><h1 className="mt-3 text-3xl font-extrabold">{reset ? 'Crie uma nova senha' : 'Redefina sua senha'}</h1><p className="mt-3 text-sm leading-6 text-[#aaa]">{reset ? 'Informe o token e uma senha com ao menos 8 caracteres.' : 'A solicitação será registrada. O envio do link será ativado com o provedor de e-mail.'}</p><div className="mt-7 space-y-4">{reset ? <><input className="h-14 w-full rounded-2xl border border-[#292929] bg-[#080808] px-4" placeholder="Token" value={token} onChange={(event) => setToken(event.target.value)} /><input className="h-14 w-full rounded-2xl border border-[#292929] bg-[#080808] px-4" placeholder="Nova senha" type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></> : <input className="h-14 w-full rounded-2xl border border-[#292929] bg-[#080808] px-4" placeholder="E-mail" type="email" value={email} onChange={(event) => setEmail(event.target.value)} />}{mutation.isSuccess && <p className="rounded-xl border border-[#c7ff3d]/30 p-3 text-sm text-[#c7ff3d]">{reset ? 'Senha alterada. Redirecionando...' : 'Se a conta existir, a solicitação foi registrada.'}</p>}{mutation.isError && <p className="rounded-xl border border-[#ff6b6b]/30 p-3 text-sm text-[#ff8b8b]">Não foi possível concluir a solicitação.</p>}<button className="h-14 w-full rounded-full bg-[#c7ff3d] font-bold text-[#080808] disabled:opacity-50" disabled={mutation.isPending || (reset ? !token.trim() || password.length < 8 : !email.trim())} onClick={() => mutation.mutate()}>{reset ? 'Alterar senha' : 'Solicitar redefinição'}</button><Link className="block text-center text-sm text-[#aaa]" to="/login">Voltar para o login</Link></div></section></main>
}
