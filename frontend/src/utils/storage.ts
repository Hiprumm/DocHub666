/**
 * localStorage 键名统一常量
 */
export const STORAGE_KEYS = {
  REMEMBER_EMAIL: 'dochub_remember_email',
} as const

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