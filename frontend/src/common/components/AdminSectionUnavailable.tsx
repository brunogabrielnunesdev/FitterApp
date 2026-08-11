import { Link } from 'react-router-dom'

type AdminSectionUnavailableProps = {
  eyebrow: string
  title: string
  description: string
}

export function AdminSectionUnavailable({
  eyebrow,
  title,
  description,
}: AdminSectionUnavailableProps) {
  return (
    <div className="mx-auto max-w-[1440px] px-6 py-12 lg:px-10 lg:py-16">
      <div className="border-b border-[#292929] pb-10">
        <p className="text-xs font-bold tracking-[.18em] text-[#c7ff3d]">{eyebrow}</p>
        <h1 className="mt-3 font-['Manrope'] text-4xl font-extrabold tracking-[-.045em] md:text-5xl">
          {title}
        </h1>
      </div>

      <section className="mt-8 rounded-[30px] border border-[#292929] bg-[#111] p-8 md:p-10">
        <span className="inline-flex rounded-full border border-[#7657ff]/35 bg-[#7657ff]/10 px-3 py-1.5 text-xs font-bold tracking-[.1em] text-[#aa9cff]">
          EM PREPARAÇÃO
        </span>
        <h2 className="mt-6 font-['Manrope'] text-2xl font-extrabold">Estrutura pronta para a próxima entrega</h2>
        <p className="mt-3 max-w-2xl text-sm leading-7 text-[#aaaaaa]">{description}</p>
        <Link
          className="mt-7 inline-flex rounded-full bg-[#f6f4ee] px-5 py-3 text-sm font-extrabold text-[#080808] transition hover:bg-[#c7ff3d]"
          to="/admin">
          Voltar ao dashboard
        </Link>
      </section>
    </div>
  )
}
