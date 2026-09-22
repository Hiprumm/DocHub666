import axios, { AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import type { ApiResult } from '@/types/auth'
import { getToken, clearAuth } from '@/utils/storage'

/**
 * Axios 实例封装。
 *
 * - baseURL 固定 `/backend`（与后端 context-path 一致），本地由 Vite 代理转发到 :10086；
 * - 请求拦截器自动注入 `Authorization: Bearer <token>`（仅当已登录）；
 * - 响应拦截器解包后端统一 `Result<T>`：业务失败（code!==200）reject 出 message，
 *   登录失效（401/403）清空本地认证态并重定向回登录页。
 */

const http = axios.create({
  baseURL: '/backend',
  timeout: 15000,
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    // 禁止浏览器/代理缓存鉴权与业务响应，防止 token/敏感体滞留缓存
    response.headers['Cache-Control'] = 'no-store'
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
    // 未登录 / 凭证过期 / 越权：清空认证态并回登录页
    if (status === 401 || status === 403) {
      clearAuth()
      if (window.location.pathname !== '/') {
        window.location.href = '/'
      }
    }
    return Promise.reject(new Error(msg || '网络异常，请稍后重试'))
  },
)

/** 泛型请求助手：兼容第三参数透传 AxiosRequestConfig */
export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  return http.request<T, T>(config)
}

export default http