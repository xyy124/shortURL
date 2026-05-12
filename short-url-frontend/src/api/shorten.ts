import request from './request'
import type { ApiResponse, ShortenResponse } from './types'

export function createShortUrl(longUrl: string) {
  return request.post<any, ApiResponse<ShortenResponse>>('/api/v1/shorten', { longUrl })
}

export function createCustomShortUrl(longUrl: string, customCode: string) {
  return request.post<any, ApiResponse<ShortenResponse>>('/api/v1/shorten', { longUrl, customCode })
}
