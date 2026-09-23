import axios, {
  AxiosError,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from 'axios'
import type { ApiResult } from '@/types/auth'
import {
  getAccessToken,
  getRefreshToken,
  updateTokens,
  clearAuth,
} from '@/utils/storage'
import { refreshApi } from './auth'

/**
 * Axios 实例封装。
 *
 * - baseURL 固定 `/backend`（与后端 context-path 一致），本地由 Vite 代理转发到 :10086；
 * - 请求拦截器自动注入 `Authorization: Bearer <accessToken>`（仅当已登录）；
 * - 响应拦截器解包后端统一 `Result<T>`：业务失败（code!==200）reject 出 message，
 *   Access Token 过期（401）时用 Refresh Token 换新并**重放原请求**（并发仅刷新一次），
 *   刷新失败或 Refresh 自身失效才清空登录态回登录页。
 */

const http = axios.create({
  baseURL: '/backend',
  timeout: 15000,
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const accessToken = getAccessToken()
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  // 禁止浏览器/代理缓存鉴权与业务响应，防止 token/敏感体滞留缓存
  config.headers['Cache-Control'] = 'no-store'
  return config
})

/** 是否单点刷新令牌的开关：并发 401 只允许一次刷新 */
let refreshPromise: Promise<void> | null = null

/**
 * 用 Refresh Token 换新一对令牌（单点串行化）。成功后更新本地存储；
 * 失败时清空登录态并抛错，交由调用方跳转。
 */
function doRefresh(): Promise<void> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    return Promise.reject(new Error('无刷新令牌'))
  }
  if (!refreshPromise) {
    refreshPromise = refreshApi({ refreshToken })
      .then((res) => {
        updateTokens(res.accessToken, res.refreshToken, res.expiresIn)
      })
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

/** 是否当前请求是刷新令牌接口（避免刷新接口自身 401 触发无限递归） */
function isRefreshRequest(config: { url?: string }): boolean {
  return (config.url ?? '').includes('/auth/refresh')
}

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown> | undefined
    // 后端统一包一层 Result，成功 code=200
    if (body && typeof body.code === 'number') {
      if (body.code === 200) {
        return body.data as never
      }
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return response.data as never
  },
  (error: AxiosError<ApiResult<unknown>>) => {
    const status = error.response?.status
    const msg = error.response?.data?.message
    const originalConfig = error.config as (AxiosRequestConfig & { _retried?: boolean }) | undefined

    // Access Token 过期：尝试用 Refresh 换新并重放原请求（跳过刷新接口与已重试请求）
    if (
      status === 401 &&
      originalConfig &&
      !originalConfig._retried &&
      !isRefreshRequest(originalConfig) &&
      getRefreshToken()
    ) {
      originalConfig._retried = true
      return doRefresh()
        .then(() =>
          http.request<unknown, unknown>(originalConfig) as Promise<unknown>,
        )
        .catch(() => {
          clearAuth()
          if (window.location.pathname !== '/') {
            window.location.href = '/'
          }
          return Promise.reject(new Error(msg || '登录已过期，请重新登录'))
        })
    }

    // 刷新令牌自身失效 / 越权：清空登录态并回登录页
    if (status === 401 || status === 403) {
      clearAuth()
      if (window.location.pathname !== '/') {
        window.location.href = '/'
      }
    }
    return Promise.reject(new Error(msg || '网络异常，请稍后重试'))
  },
)

/** 泛型请求助手：响应拦截器已解包后端 Result，故可直接返回业务数据类型 T */
export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  // axios 签名的返回类型为 AxiosResponse；但响应拦截器已解包 body.data，故在此断言为业务 T
  return http.request<unknown, T>(config) as unknown as Promise<T>
}

export default http