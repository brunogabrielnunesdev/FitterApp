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
  profilesApproved: number
  profilesRejected: number
}

export type EventMetrics = {
  raw: number
  unique: number
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
