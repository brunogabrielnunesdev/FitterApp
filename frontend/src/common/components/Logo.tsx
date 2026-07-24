import logo from '../../assets/fitterapp-logo.png'

type LogoProps = {
  compact?: boolean
}

export function Logo({ compact = false }: LogoProps) {
  return (
    <div className="flex items-center gap-3">
      <img alt="FitterApp" className={compact ? 'h-10 w-10' : 'h-14 w-14'} src={logo} />
      <span className="font-['Manrope'] text-sm font-extrabold tracking-[0.22em] text-[#f6f4ee]">
        FITTERAPP
      </span>
    </div>
  )
}
