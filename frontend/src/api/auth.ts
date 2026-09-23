import { request } from './http'
import type {
  LoginPayload,
  LoginResult,
  PublicKeyResp,
  RefreshPayload,
  RefreshResult,
} from '@/types/auth'

/** 登录：POST /backend/auth/login */
export function loginApi(payload: LoginPayload): Promise<LoginResult> {
  return request<LoginResult>({
    url: '/auth/login',
    method: 'POST',
    data: payload,
  })
}

/** 刷新令牌：POST /backend/auth/refresh（Refresh Token 轮换为新一对令牌） */
export function refreshApi(payload: RefreshPayload): Promise<RefreshResult> {
  return request<RefreshResult>({
    url: '/auth/refresh',
    method: 'POST',
    data: payload,
  })
}

/** 获取 RSA 公钥：GET /backend/auth/public-key（用于登录密码加密，未启用则为 null） */
export function getPublicKeyApi(): Promise<PublicKeyResp> {
  return request<PublicKeyResp>({
    url: '/auth/public-key',
    method: 'GET',
  })
}

/** 登出：POST /backend/auth/logout（使当前会话失效） */
export function logoutApi(): Promise<void> {
  return request<void>({
    url: '/auth/logout',
    method: 'POST',
  })
}