import request from './request'
import type { ApiResponse, StatsOverview, DailyStatsItem } from './types'

export function getOverview() {
  return request.get<any, ApiResponse<StatsOverview>>('/api/v1/admin/stats/overview')
}

export function getDailyStats(params: { shortCode?: string; days?: number }) {
  return request.get<any, ApiResponse<DailyStatsItem[]>>('/api/v1/admin/stats/daily', { params })
}
