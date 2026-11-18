export type DashboardPeriod = {
  from: string
  to: string
  timezone: string
  startInclusive: string
  endExclusive: string
}

export type FunnelMetrics = {
  accountsRegistered: number
  accountsCompleted: number
  profilesStarted: number
  profilesSubmitted: number
  profilesApproved: number | null
  profilesRejected: number | null
}

export type EventMetrics = {
  raw: number | null
  unique: number | null
}

export type AdminDashboard = {
  period: DashboardPeriod
  funnel: FunnelMetrics
  searches: EventMetrics
  profileViews: EventMetrics
  whatsappContacts: EventMetrics
  returns: EventMetrics
  filters: { modality: number; neighborhood: number; serviceMode: number }
  trackingStartedAt: string
}

export type DashboardRange = {
  from: string
  to: string
}
