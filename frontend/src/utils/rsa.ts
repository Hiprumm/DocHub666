/**
 * RSA 加密工具 —— 登录密码加密传输（配合后端 RsaUtil 的 RSA/ECB/OAEPWithSHA-256AndMGF1Padding）。
 *
 * 前端使用浏览器原生 Web Crypto API（crypto.subtle），无需新增依赖：
 *   1. 将后端下发的 SPKI Base64 公钥导入为 CryptoKey（RSA-OAEP，SHA-256）；
 *   2. 用公钥加密密码，返回 Base64 密文提交后端；
 *   3. 公钥缺失/加密失败时抛出，由调用方降级为明文传输。
 */

function base64ToArrayBuffer(base64: string): ArrayBuffer {
  const binary = atob(base64.replace(/\s+/g, ''))
  const len = binary.length
  const bytes = new Uint8Array(len)
  for (let i = 0; i < len; i++) {
    bytes[i] = binary.charCodeAt(i)
  }
  return bytes.buffer
}

async function importPublicKey(spkiBase64: string): Promise<CryptoKey> {
  return crypto.subtle.importKey(
    'spki',
    base64ToArrayBuffer(spkiBase64),
    { name: 'RSA-OAEP', hash: 'SHA-256' },
    true,
    ['encrypt'],
  )
}

function arrayBufferToBase64(buffer: ArrayBuffer): string {
  const bytes = new Uint8Array(buffer)
  let binary = ''
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i])
  }
  return btoa(binary)
}

/**
 * 用 RSA 公钥加密明文密码，返回 Base64 密文。
 * 失败（Web Crypto 不可用 / 公钥无效）时抛错，调用方应降级为明文提交。
 */
export async function encryptPassword(plainPassword: string, spkiBase64: string): Promise<string> {
  if (!plainPassword || !spkiBase64) {
    throw new Error('缺少密码或公钥，无法加密')
  }
  const key = await importPublicKey(spkiBase64)
  const cipher = await crypto.subtle.encrypt(
    { name: 'RSA-OAEP' },
    key,
    new TextEncoder().encode(plainPassword),
  )
  return arrayBufferToBase64(cipher)
}