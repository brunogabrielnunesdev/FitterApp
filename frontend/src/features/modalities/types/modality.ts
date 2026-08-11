export type AdminModality = {
  id: number
  name: string
  slug: string
  active: boolean
}

export type ModalityStatusFilter = 'ALL' | 'ACTIVE' | 'INACTIVE'
