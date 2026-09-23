/**
 * 认证领域类型定义——与后端 auth/dto 契约 100% 对齐（GLOSSARY 禁止脑补字段）。
 */

/** 登录请求体（对齐后端 LoginReq：账号名 username + 密码 password） */
export interface LoginPayload {
  username: string
  password: string
}

/** 登录成功响应体（对齐后端 LoginResp：双 token + 用户展示信息） */
export interface LoginResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userId: number
  username: string
  realName: string | null
  roleKey: string | null
}

/** 刷新令牌请求体（对齐后端 RefreshReq） */
export interface RefreshPayload {
  refreshToken: string
}

/** 刷新令牌响应体（对齐后端 RefreshResp：Refresh 已轮换为新值） */
export interface RefreshResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

/** RSA 公钥响应体（对齐后端 PublicKeyResp） */
export interface PublicKeyResp {
  publicKey: string | null
}

/** 后端统一响应包装（对齐 common.Result<T>，code=200 表示成功） */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}