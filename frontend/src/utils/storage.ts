import type { LoginResult } from '@/types/auth'

/**
 * localStorage 键名统一常量
 */
export const STORAGE_KEYS = {
  REMEMBER_EMAIL: 'dochub_remember_email',
  AUTH: 'dochub_auth',
} as const

/** 持久化保存的认证信息（脱敏，含双 token 与展示字段，绝不存密码） */
export interface StoredAuth {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userId: number
  username: string
  realName: string | null
  roleKey: string | null
}

/**
 * 持久化“记住我”邮箱（勾选时才写入，未勾选则清除）
 */
export function setRememberedEmail(email: string, remember: boolean): void {
  if (remember) {
    localStorage.setItem(STORAGE_KEYS.REMEMBER_EMAIL, email)
  } else {
    localStorage.removeItem(STORAGE_KEYS.REMEMBER_EMAIL)
  }
}

/**
 * 读取已记住的邮箱（不存在则返回空串）
 */
export function getRememberedEmail(): string {
  return localStorage.getItem(STORAGE_KEYS.REMEMBER_EMAIL) ?? ''
}

/**
 * 保存登录态（登录成功后调用）。token 字段兼容旧结构（单 token），
 * 若缺少 accessToken 则将单独保存的 token 回填为 accessToken。
 */
export function setAuth(auth: LoginResult): void {
  const stored: StoredAuth = {
    accessToken: auth.accessToken,
    refreshToken: auth.refreshToken,
    expiresIn: auth.expiresIn,
    userId: auth.userId,
    username: auth.username,
    realName: auth.realName,
    roleKey: auth.roleKey,
  }
  localStorage.setItem(STORAGE_KEYS.AUTH, JSON.stringify(stored))
}

/**
 * 更新已持久化的双 token（刷新成功后调用，保留其余展示字段）。
 */
export function updateTokens(accessToken: string, refreshToken: string, expiresIn: number): void {
  const auth = getAuth()
  if (!auth) return
  const stored: StoredAuth = {
    ...auth,
    accessToken,
    refreshToken,
    expiresIn,
  }
  localStorage.setItem(STORAGE_KEYS.AUTH, JSON.stringify(stored))
}

/**
 * 读取登录态（未登录返回 null）
 */
export function getAuth(): StoredAuth | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.AUTH)
    if (!raw) return null
    const parsed = JSON.parse(raw) as StoredAuth
    return parsed.accessToken ? parsed : null
  } catch {
    return null
  }
}

/**
 * 读取访问令牌（未登录返回空串）
 */
export function getAccessToken(): string {
  return getAuth()?.accessToken ?? ''
}

/**
 * 读取刷新令牌（未登录返回空串）
 */
export function getRefreshToken(): string {
  return getAuth()?.refreshToken ?? ''
}

/**
 * 兼容旧名：读取访问令牌（未登录返回空串）
 */
export function getToken(): string {
  return getAccessToken()
}

/**
 * 当前是否已登录（存在有效 token）
 */
export function isLoggedIn(): boolean {
  return getAccessToken() !== ''
}

/**
 * 清空登录态（登出 / 凭证过期 401/403 时调用）
 */
export function clearAuth(): void {
  localStorage.removeItem(STORAGE_KEYS.AUTH)
}