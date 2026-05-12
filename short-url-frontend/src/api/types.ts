export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T> {
  total: number
  page: number
  size: number
  records: T[]
}

export interface ShortenResponse {
  shortCode: string
  shortUrl: string
  longUrl: string
}

export interface LoginResponse {
  token: string
  user: UserVO
}

export interface UserVO {
  id: number
  username: string
  nickname: string
  role: string
}

export interface StatsOverview {
  totalUrls: number
  totalViews: number
  todayNewUrls: number
  todayViews: number
}

export interface DailyStatsItem {
  statsDate: string
  pv: number
  uv: number
  ipCount: number
}

export interface UrlMapRecord {
  id: number
  shortCode: string
  longUrl: string
  userId: number
  views: number
  isCustom: boolean
  isActive: boolean
  expireTime: string | null
  createTime: string
  updateTime: string
}
