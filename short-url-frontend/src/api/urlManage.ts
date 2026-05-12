import request from './request'
import type { ApiResponse, PageResult, UrlMapRecord } from './types'

export function getUrlList(params: { page: number; size: number }) {
  return request.get<any, ApiResponse<PageResult<UrlMapRecord>>>('/api/v1/admin/urls', { params })
}

export function deleteUrl(id: number) {
  return request.delete<any, ApiResponse<null>>(`/api/v1/admin/urls/${id}`)
}

export function toggleUrlActive(id: number) {
  return request.put<any, ApiResponse<null>>(`/api/v1/admin/urls/${id}/toggle`)
}

export function getAllUrls() {
  return request.get<any, ApiResponse<UrlMapRecord[]>>('/api/v1/admin/urls/all')
}
