import request from './http'
import type { LoginPayload, LoginResult } from '@/types/auth'

/** 登录：POST /backend/auth/login */
export function loginApi(payload: LoginPayload): Promise<LoginResult> {
  return request<LoginResult>({
    url: '/auth/login',
    method: 'POST',
    data: payload,
  })
}