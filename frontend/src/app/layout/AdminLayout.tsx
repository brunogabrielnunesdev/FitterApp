import { useState } from 'react'
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom'

import { Logo } from '../../common/components/Logo'
import { useAuth } from '../../features/auth/context/useAuth'

const navigation = [
  { label: 'Dashboard', to: '/admin', matches: (path: string) => path === '/admin' },
  {
    label: 'Perfis',
    to: '/admin/personals/pending',
    matches: (path: string) => path.startsWith('/admin/personals'),
  },
  {
    label: 'Usuários',
    to: '/admin/users',
    matches: (path: string) => path.startsWith('/admin/users'),
  },
  {
    label: 'Modalidades',
    to: '/admin/modalities',
    matches: (path: string) => path.startsWith('/admin/modalities'),
  },
]

export function AdminLayout() {
  const { email, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [isLoggingOut, setIsLoggingOut] = useState(false)

  async function handleLogout() {
    setIsLoggingOut(true)
    try {
      await logout()
      navigate('/login', { replace: true })
    } finally {
      setIsLoggingOut(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#080808] text-[#f6f4ee]">
      <a
        className="fixed left-4 top-3 z-50 -translate-y-20 rounded-lg bg-[#c7ff3d] px-4 py-2 text-sm font-bold text-[#080808] transition focus:translate-y-0"
        href="#admin-content">
        Ir para o conteúdo
      </a>

      <header className="sticky top-0 z-40 border-b border-[#292929] bg-[#080808]/95 backdrop-blur-xl">
        <div className="mx-auto flex h-20 max-w-[1440px] items-center justify-between gap-5 px-5 sm:px-6 lg:px-10">
          <Link aria-label="FitterApp — dashboard administrativo" to="/admin">
            <Logo compact />
          </Link>

          <div className="flex min-w-0 items-center gap-3 sm:gap-5">
            <div className="hidden min-w-0 text-right sm:block">
              <p className="text-[10px] font-bold tracking-[.14em] text-[#777]">CONTA ADMIN</p>
              <p className="mt-1 max-w-56 truncate text-sm text-[#aaaaaa]" title={email ?? undefined}>
                {email}
              </p>
            </div>
            <button
              className="rounded-full border border-[#333] px-4 py-2.5 text-sm font-bold transition hover:border-[#c7ff3d] hover:text-[#c7ff3d] disabled:cursor-wait disabled:opacity-50 sm:px-5"
              disabled={isLoggingOut}
              onClick={() => void handleLogout()}
              type="button">
              {isLoggingOut ? 'Saindo…' : 'Sair'}
            </button>
          </div>
        </div>

        <nav
          aria-label="Navegação administrativa"
          className="mx-auto max-w-[1440px] overflow-x-auto px-2 sm:px-4 lg:px-8">
          <ul className="flex min-w-max gap-1 overflow-x-auto px-1 sm:min-w-0">
            {navigation.map((item) => {
              const active = item.matches(location.pathname)
              return (
                <li key={item.to}>
                  <Link
                    aria-current={active ? 'page' : undefined}
                    className={`relative flex h-12 items-center px-3 text-sm font-bold transition sm:px-4 ${
                      active ? 'text-[#f6f4ee]' : 'text-[#777] hover:text-[#c7c4bd]'
                    }`}
                    to={item.to}>
                    {item.label}
                    {active && (
                      <span className="absolute inset-x-3 bottom-0 h-0.5 rounded-full bg-[#c7ff3d] sm:inset-x-4" />
                    )}
                  </Link>
                </li>
              )
            })}
          </ul>
        </nav>
      </header>

      <main id="admin-content">
        <Outlet />
      </main>
    </div>
  )
}
