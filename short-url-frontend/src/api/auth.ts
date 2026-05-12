import request from './request'
import type { ApiResponse, LoginResponse } from './types'

export function register(username: string, password: string, nickname: string) {
  return request.post<any, ApiResponse<LoginResponse>>('/api/v1/auth/register', {
    username, password, nickname,
  })
}

export function login(username: string, password: string) {
  return request.post<any, ApiResponse<LoginResponse>>('/api/v1/auth/login', {
    username, password,
  })
}
