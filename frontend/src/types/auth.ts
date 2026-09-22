/**
 * 认证领域类型定义——与后端 auth/dto 契约 100% 对齐（GLOSSARY 禁止脑补字段）。
 */

/** 登录请求体（对齐后端 LoginReq：账号名 username + 密码 password） */
export interface LoginPayload {
  username: string
  password: string
}

/** 登录成功响应体（对齐后端 LoginResp） */
export interface LoginResult {
  token: string
  userId: number
  username: string
  realName: string | null
  roleKey: string | null
}

/** 后端统一响应包装（对齐 common.Result<T>，code=200 表示成功） */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}