import request from './request'
import type { LoginResult, UserInfo } from '@/types'

export interface LoginParams {
  username: string
  password: string
}

export function login(data: LoginParams) {
  return request.post<unknown, LoginResult>('/auth/login', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getMe() {
  return request.get<unknown, UserInfo>('/auth/me')
}
