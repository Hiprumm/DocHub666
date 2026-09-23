<script setup lang="ts">
/**
 * LoginView —— 登录页正式暖棕「纸质笔记本」版
 * 布局：左书脊氛围区（暖棕 #492D22）+ 右侧呼吸书页表单（浅纸 #F0E8DD）。
 * 业务逻辑完整保留：用户名/密码校验、RSA 密码加密传输（公钥可用则加密，
 * 否则明文回退）、记住我持久化、loading/服务器错误提示、登录后跳转仪表盘。
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getRememberedEmail, setRememberedEmail, setAuth } from '@/utils/storage'
import { MIN_PASSWORD_LENGTH } from '@/utils/validation'
import { loginApi, getPublicKeyApi } from '@/api/auth'
import { encryptPassword } from '@/utils/rsa'

const router = useRouter()

interface LoginForm {
  username: string
  password: string
  remember: boolean
}

const form = reactive<LoginForm>({
  username: '',
  password: '',
  remember: false,
})
const showPassword = ref(false)
const loading = ref(false)
const serverError = ref('')

const errors = reactive<{ username: string; password: string }>({
  username: '',
  password: '',
})

// 勾选“记住我”则回填已存用户名
onMounted(() => {
  const saved = getRememberedEmail()
  if (saved) {
    form.username = saved
    form.remember = true
  }
})

function validateField(field: 'username' | 'password'): boolean {
  if (field === 'username') {
    errors.username = form.username.trim() ? '' : '请输入用户名'
    return !errors.username
  }
  errors.password = form.password
    ? form.password.length >= MIN_PASSWORD_LENGTH
      ? ''
      : `密码长度不能少于 ${MIN_PASSWORD_LENGTH} 位`
    : '请输入密码'
  return !errors.password
}

const canSubmit = computed(
  () => form.username.trim() !== '' && form.password !== '',
)

/**
 * 提交：校验 → 尝试拉取公钥对密码 RSA 加密 → /auth/login → 存 token 跳仪表盘。
 */
async function handleSubmit() {
  if (loading.value) return
  serverError.value = ''
  const nameOk = validateField('username')
  const pwdOk = validateField('password')
  if (!nameOk || !pwdOk) return

  const username = form.username.trim()
  setRememberedEmail(username, form.remember)

  loading.value = true
  try {
    let password = form.password
    try {
      const pub = await getPublicKeyApi()
      if (pub.publicKey) {
        password = await encryptPassword(form.password, pub.publicKey)
      }
    } catch {
      // 公钥拉取失败不影响登录，回落明文
    }
    const result = await loginApi({ username, password })
    setAuth(result)
    void router.push('/dashboard')
  } catch (err) {
    serverError.value =
      err instanceof Error ? err.message : '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="paper-login">
    <!-- 左：书籍扉页氛围区 -->
    <section class="marker" aria-labelledby="login-headline">
      <div class="marker-top">
        <span class="marker-logo" aria-hidden="true">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="3" stroke="currentColor" stroke-width="1" />
            <path d="M9 8h9l5 5v11H9z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round" />
            <path d="M18 8v5h5" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round" />
            <path d="M13 19h7M13 22h5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" />
          </svg>
        </span>
        <p class="marker-kicker">DocHub</p>
      </div>

      <h1 id="login-headline" class="marker-title">
        把知识，<br>写进纸页里。
      </h1>
      <p class="marker-blurb">
        一个安静的企业文档平台。检索、阅读、协作，都像翻开一本装帧考究的书——
        工具退到背景，让人回到内容本身。
      </p>
      <div class="marker-rule" aria-hidden="true" />
      <p class="marker-note">为长时间阅读与编辑的知识工作者而作</p>
    </section>

    <!-- 右：书页式登录表单 -->
    <section class="form-wrap" aria-label="登录表单">
      <form class="form" novalidate @submit.prevent="handleSubmit">
        <p class="form-kicker">登录到你的工作空间</p>
        <h2 class="form-title">欢迎回来</h2>

        <label class="field">
          <span class="field-lbl">账号</span>
          <input
            v-model="form.username"
            class="field-ip"
            type="text"
            name="username"
            autocomplete="username"
            placeholder="你的用户名"
            @blur="validateField('username')"
          >
          <span v-if="errors.username" class="field-err" role="alert">{{ errors.username }}</span>
        </label>

        <label class="field">
          <span class="field-lbl">密码</span>
          <div class="field-ip-wrap">
            <input
              v-model="form.password"
              class="field-ip field-ip--pass"
              :type="showPassword ? 'text' : 'password'"
              name="password"
              autocomplete="current-password"
              placeholder="至少 8 位"
              @blur="validateField('password')"
            >
            <button
              type="button"
              class="field-eye"
              :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              :aria-pressed="showPassword"
              @click="showPassword = !showPassword"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round" />
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.4" />
              </svg>
            </button>
          </div>
          <span v-if="errors.password" class="field-err" role="alert">{{ errors.password }}</span>
        </label>

        <div class="form-options">
          <label class="check">
            <input v-model="form.remember" type="checkbox" class="check__box">
            <span class="check__label">记住我</span>
          </label>
        </div>

        <p v-if="serverError" class="form-error" role="alert">{{ serverError }}</p>

        <button
          class="submit"
          type="submit"
          :disabled="loading || !canSubmit"
        >
          {{ loading ? '登录中…' : '进入工作空间' }}
        </button>

        <p class="form-foot">需要帮助？联系你的系统管理员。</p>
      </form>
      <p class="form-version">DocHub v0.1.0</p>
    </section>
  </main>
</template>

<style scoped>
/* =====================================================================
 * 登录页 —— 暖棕「纸质笔记本」，色值全部走全局 Token（tokens.css）
 * 不在此硬编码任何颜色。
 * ===================================================================== */
.paper-login {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 0;
  min-height: 100vh;
  background: var(--color-bg-base);
  color: var(--color-text-primary);
  font-family: var(--font-serif);
  -webkit-font-smoothing: antialiased;
}

/* ---------- 左：书脊氛围 ---------- */
.marker {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-6);
  padding: clamp(40px, 8vw, 96px);
  background: var(--color-accent-mark);
  color: var(--color-text-on-accent);
}
.marker-top {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  color: var(--color-text-on-accent);
}
.marker-logo {
  display: inline-flex;
  opacity: 0.9;
}
.marker-kicker {
  margin: 0;
  font-size: 15px;
  letter-spacing: 0.18em;
  opacity: 0.82;
}
.marker-title {
  margin: 0;
  font-family: var(--font-serif);
  font-weight: 500;
  font-size: clamp(34px, 5vw, 52px);
  line-height: 1.25;
  letter-spacing: 0.01em;
}
.marker-blurb {
  margin: 0;
  max-width: 42ch;
  font-size: 17px;
  line-height: 1.85;
  opacity: 0.86;
}
.marker-rule {
  width: 56px;
  height: 2px;
  background: var(--color-text-on-accent);
  opacity: 0.7;
}
.marker-note {
  margin: 0;
  font-size: 14px;
  opacity: 0.6;
}

/* ---------- 右：书页表单 ---------- */
.form-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: var(--space-8);
  padding: clamp(32px, 6vw, 72px);
  background:
    radial-gradient(120% 90% at 70% -10%, rgba(255, 255, 255, 0.55), transparent 55%),
    var(--color-bg-canvas);
}
.form {
  width: 100%;
  max-width: 380px;
}
.form-kicker {
  margin: 0 0 10px;
  font-size: 14px;
  letter-spacing: 0.12em;
  color: var(--color-text-tertiary);
}
.form-title {
  margin: 0 0 36px;
  font-family: var(--font-serif);
  font-weight: 500;
  font-size: 30px;
  line-height: 1.3;
}

/* ---------- 字段 ---------- */
.field {
  display: block;
  margin-bottom: 22px;
}
.field-lbl {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--color-accent);
}
.field-ip {
  width: 100%;
  padding: 12px 14px;
  font-family: inherit;
  font-size: 16px;
  color: var(--color-text-primary);
  background: var(--color-surface-card);
  border: 1px solid var(--color-border-input);
  border-radius: var(--radius-xs);
  transition: border-color 180ms ease, box-shadow 180ms ease;
}
.field-ip::placeholder {
  color: var(--color-text-placeholder);
}
.field-ip:focus {
  outline: none;
  border-color: var(--color-accent);
  box-shadow: var(--glow-input-focus);
}
.field-ip-wrap {
  position: relative;
}
.field-ip--pass {
  padding-right: 44px;
}
.field-eye {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: var(--radius-xs);
  background: transparent;
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition: color 180ms ease;
}
.field-eye:hover {
  color: var(--color-accent);
}
.field-err {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-error);
}

/* ---------- 记住我 ---------- */
.form-options {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
.check {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
}
.check__box {
  appearance: none;
  width: 15px;
  height: 15px;
  margin: 0;
  border: 1px solid var(--color-border-strong);
  border-radius: 2px;
  background: var(--color-surface-card);
  cursor: pointer;
  position: relative;
  transition: background-color var(--dur-fast);
}
.check__box:hover {
  border-color: var(--color-accent);
}
.check__box:checked {
  background-color: var(--color-accent);
  border-color: var(--color-accent);
}
.check__box:checked::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 1px;
  width: 4px;
  height: 8px;
  border: solid var(--color-text-on-accent);
  border-width: 0 1.6px 1.6px 0;
  transform: rotate(45deg);
}
.check__label {
  font-size: 14px;
  color: var(--color-text-tertiary);
}

/* ---------- 服务器错误 ---------- */
.form-error {
  margin: 0 0 var(--space-2);
  font-size: 14px;
  color: var(--color-error);
  line-height: 1.5;
}

/* ---------- 主按钮 ---------- */
.submit {
  width: 100%;
  margin-top: var(--space-2);
  padding: 13px 16px;
  font-family: inherit;
  font-size: 16px;
  color: var(--color-btn-primary-fg);
  background: var(--color-btn-primary-bg);
  border: none;
  border-radius: var(--radius-xs);
  cursor: pointer;
  transition: background-color 180ms ease;
}
.submit:hover:not(:disabled) {
  background: var(--color-btn-primary-bg-hover);
}
.submit:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
.form-foot {
  margin: 26px 0 0;
  font-size: 13px;
  text-align: center;
  color: var(--color-text-tertiary);
}
.form-version {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.06em;
  color: var(--color-text-placeholder);
}

/* ---------- 窄屏 ---------- */
@media (max-width: 760px) {
  .paper-login {
    grid-template-columns: 1fr;
  }
  .marker {
    padding: 44px 28px;
    gap: var(--space-4);
  }
  .marker-blurb,
  .marker-note,
  .marker-rule {
    display: none;
  }
  .marker-title {
    font-size: 30px;
  }
  .form-wrap {
    padding: 48px 28px;
  }
}

/* 入场动效：书页内容淡入上移，总时长 ≤500ms，尊重 reduced-motion */
@media (prefers-reduced-motion: no-preference) {
  .marker,
  .form-wrap {
    animation: fade-up var(--dur-slow) var(--ease-out) both;
  }
  .form-wrap {
    animation-delay: 0.12s;
  }
}
@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>