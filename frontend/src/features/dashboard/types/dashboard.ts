export type DashboardPeriod = {
  from: string
  to: string
  timezone: string
  startInclusive: string
  endExclusive: string
}

export type FunnelMetrics = {
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
}

export type DashboardRange = {
  from: string
  to: string
}
