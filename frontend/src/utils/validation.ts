/**
 * 登录表单校验工具（纯函数，便于测试）
 */

// 邮箱格式正则（简化、实用）
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function isValidEmail(email: string): boolean {
  return EMAIL_REGEX.test(email.trim())
}

// 密码最少位数（与产品约束一致）
export const MIN_PASSWORD_LENGTH = 8

export function isPasswordValid(password: string): boolean {
  return password.length >= MIN_PASSWORD_LENGTH
}