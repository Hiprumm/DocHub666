<script setup lang="ts">
/**
 * LoginView —— 登录页（“去 AI 味 · 深海编辑感”重构）
 * 视觉依据：docs/02-design/DESIGN_SYSTEM.md + 本页构成设计要求
 * 业务功能逻辑保持不变：邮箱/密码校验、密码显隐、记住我持久化、模拟登录跳转。
 *
 * 布局：桌面 ≥1024px 左右分栏（左 55% 品牌叙事 / 右 45% 表单）；
 *       <1024px 折叠为顶部紧凑 Banner + 表单全宽。
 * 色规纪律：#D1FFFF 唯一强调色（≤5%）；正文 #AAD9F2；次文本 #618EA5；
 *           placeholder #85B3CB；边框/发丝线 #134155。
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseInput from '@/components/BaseInput.vue'
import BaseButton from '@/components/BaseButton.vue'
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
// 后端返回的错误提示（账号/密码错误、停用等）
const serverError = ref('')

const errors = reactive<{ username: string; password: string }>({
  username: '',
  password: '',
})

// 勾选“记住我”则回填已存邮箱（用户名）
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
 * 提交：校验 → 调用后端 /auth/login → 成功存 token 跳仪表盘；失败展示后端错误。
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
    // RSA 加密：若后端下发了公钥则加密密码提交，否则明文回退
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
  <main class="login-page">
    <!-- 深海蓝径向渐变光斑（透明度 ≤8%，非大面积发光） -->
    <div class="login-glow" aria-hidden="true">
      <span class="login-glow__dot login-glow__dot--1" />
      <span class="login-glow__dot login-glow__dot--2" />
      <span class="login-glow__dot login-glow__dot--3" />
    </div>
    <!-- SVG film grain 噪点层（opacity 3–5%） -->
    <div class="login-grain" aria-hidden="true" />

    <div class="login-layout">
      <!-- 品牌区：桌面为左栏叙事，移动端折叠为顶部 Banner -->
      <section class="login-brand" aria-labelledby="login-headline">
        <div class="login-brand__banner">
          <span class="login-brand__logo" aria-hidden="true">
            <svg width="34" height="34" viewBox="0 0 32 32" fill="none">
              <rect width="32" height="32" rx="7" stroke="var(--color-accent-mark)" stroke-width="1" />
              <path
                d="M9 8h9l5 5v11H9z"
                stroke="var(--color-accent-mark)"
                stroke-width="1.4"
                stroke-linejoin="round"
              />
              <path
                d="M18 8v5h5"
                stroke="var(--color-accent-mark)"
                stroke-width="1.4"
                stroke-linejoin="round"
              />
              <path d="M13 19h7M13 22h5" stroke="var(--color-accent-mark)" stroke-width="1.2" stroke-linecap="round" />
            </svg>
          </span>
          <span class="login-brand__name">DocHub</span>
        </div>

        <!-- 桌面端展示区 -->
        <div class="login-brand__body">
          <h1 id="login-headline" class="login-headline">知识，沉得住</h1>
          <p class="login-value">让每一份文档，都有归处、有脉络、可被唤醒。</p>
          <p class="login-value">部门空间沉淀协作，「查看 → 检索 → 合成」驱动团队的文档复利。</p>

          <!-- 文档缩略线框网格装饰（发丝线 #134155，≤15%） -->
          <div class="login-frames" aria-hidden="true">
            <span v-for="n in 6" :key="n" class="login-frames__doc">
              <span class="login-frames__bar" />
              <span class="login-frames__line" />
              <span class="login-frames__line" />
              <span class="login-frames__line login-frames__line--short" />
            </span>
          </div>
        </div>
      </section>

      <!-- 表单区：无卡片容器，靠明度分层 #001325 -->
      <section class="login-form-panel" aria-label="登录表单">
        <form class="login-form" novalidate @submit.prevent="handleSubmit">
          <BaseInput
            id="login-username"
            v-model="form.username"
            label="USERNAME"
            type="text"
            placeholder="请输入用户名"
            autocomplete="username"
            :error="errors.username"
            @blur="validateField('username')"
          />
          <BaseInput
            id="login-password"
            v-model="form.password"
            label="PASSWORD"
            :type="showPassword ? 'text' : 'password'"
            placeholder="至少 8 位"
            autocomplete="current-password"
            :error="errors.password"
            @blur="validateField('password')"
          >
            <template #suffix>
              <button
                type="button"
                class="login-password-toggle"
                :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                :aria-pressed="showPassword"
                @click="showPassword = !showPassword"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path
                    d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z"
                    stroke="currentColor"
                    stroke-width="1.4"
                    stroke-linejoin="round"
                  />
                  <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.4" />
                </svg>
              </button>
            </template>
          </BaseInput>

          <!-- 记住我 -->
          <div class="login-options">
            <label class="login-check">
              <input v-model="form.remember" type="checkbox" class="login-check__box">
              <span class="login-check__label">记住我</span>
            </label>
          </div>

          <p v-if="serverError" class="login-error" role="alert">{{ serverError }}</p>

          <BaseButton
            type="submit"
            variant="primary"
            size="lg"
            :loading="loading"
            :disabled="!canSubmit"
            class="login-submit"
          >
            {{ loading ? '登录中…' : '登 录' }}
          </BaseButton>

        </form>

        <!-- 版本信息（等宽字体） -->
        <p class="login-version">DocHub v0.1.0 · build 0x0a1f</p>
      </section>
    </div>
  </main>
</template>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: stretch;
  background-color: var(--color-bg-base); /* #000515 */
  overflow: hidden;
}

/* ---------- 深海蓝径向渐变光斑（≤8%） ---------- */
.login-glow {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}
.login-glow__dot {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
}
.login-glow__dot--1 {
  width: 480px;
  height: 480px;
  top: -160px;
  right: 8%;
  background: rgba(0, 42, 61, 0.08); /* #002A3D ≤8% */
}
.login-glow__dot--2 {
  width: 420px;
  height: 420px;
  bottom: -140px;
  left: 30%;
  background: rgba(19, 65, 85, 0.07); /* #134155 ≤8% */
}
.login-glow__dot--3 {
  width: 300px;
  height: 300px;
  top: 40%;
  left: -120px;
  background: rgba(0, 19, 37, 0.08); /* #001325 ≤8% */
}

/* ---------- SVG film grain 噪点层（opacity ~4%） ---------- */
.login-grain {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  opacity: 0.04;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='160' height='160'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='1'/%3E%3C/svg%3E");
}

/* ---------- 左右分栏布局 ---------- */
.login-layout {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: 55% 45%;
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
}

/* ---------- 品牌区（左 55%） ---------- */
.login-brand {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: var(--space-16);
  gap: var(--space-12);
}
.login-brand__banner {
  display: none; /* 桌面隐藏，移动端才显示 */
}
.login-brand__logo {
  display: inline-flex;
}
.login-brand__body {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  max-width: 460px;
}
.login-headline {
  margin: 0;
  font-family: var(--font-display);
  font-size: 56px;
  font-weight: 700;
  line-height: 1.08;
  letter-spacing: -0.02em;
  color: var(--color-text-secondary); /* #AAD9F2，非纯白 */
}
.login-value {
  margin: 0;
  font-size: var(--text-body-lg);
  line-height: 1.6;
  color: var(--color-text-secondary); /* #AAD9F2 */
}

/* 文档缩略线框网格（发丝线） */
.login-frames {
  margin-top: var(--space-8);
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
  max-width: 420px;
}
.login-frames__doc {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: var(--space-3);
  border: 1px solid rgba(19, 65, 85, 0.13); /* #134155 ≤15% */
  border-radius: var(--radius-xs);
  min-height: 118px;
}
.login-frames__bar {
  height: 1px;
  width: 40%;
  background: rgba(19, 65, 85, 0.15);
}
.login-frames__line {
  height: 1px;
  width: 100%;
  background: rgba(19, 65, 85, 0.13);
}
.login-frames__line--short {
  width: 62%;
}

/* ---------- 表单区（右 45%，无卡片，靠明度分层） ---------- */
.login-form-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-8);
  padding: var(--space-16) var(--space-16) var(--space-8);
  background-color: var(--color-surface-card); /* #001325 局部底 */
  box-shadow: -16px 0 40px rgba(0, 13, 24, 0.5); /* 同色温 tinted shadow */
  max-width: 620px;
}
.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  max-width: 360px;
  width: 100%;
}

/* ---------- 密码显隐切换（图标按钮） ---------- */
.login-password-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-xs);
  background: transparent;
  color: var(--color-text-muted); /* #618EA5 图标/次要文字 */
  cursor: pointer;
  transition:
    color var(--dur-fast) var(--ease-out),
    background-color var(--dur-fast) var(--ease-out);
}
.login-password-toggle:hover {
  color: var(--color-accent-mark); /* #D1FFFF 唯一强调 hover */
  background-color: var(--color-surface-card);
}

/* ---------- 记住我 复选框 ---------- */
.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.login-check {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
}
.login-check__box {
  appearance: none;
  width: 15px;
  height: 15px;
  margin: 0;
  border: 1px solid var(--color-border-default); /* #2E5A6F */
  border-radius: 2px;
  background-color: transparent;
  cursor: pointer;
  position: relative;
  transition: background-color var(--dur-fast) var(--ease-out);
}
.login-check__box:hover {
  border-color: var(--color-border-strong);
}
.login-check__box:checked {
  background-color: var(--color-accent-mark); /* #D1FFFF 少量强调 */
  border-color: var(--color-accent-mark);
}
.login-check__box:checked::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 1px;
  width: 4px;
  height: 8px;
  border: solid var(--color-bg-base);
  border-width: 0 1.6px 1.6px 0;
  transform: rotate(45deg);
}
.login-check__label {
  font-family: var(--font-mono);
  font-size: var(--text-micro);
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--color-text-muted); /* #618EA5 次要文字 */
}

/* ---------- 服务器返回的错误提示 ---------- */
.login-error {
  margin: 0;
  font-family: var(--font-sans);
  font-size: var(--text-body-sm);
  color: var(--color-danger, #e5738f);
  line-height: 1.5;
}

/* ---------- 主按钮全宽 ---------- */
.login-submit {
  width: 100%;
  margin-top: var(--space-2);
}

/* ---------- 版本信息（等宽字，12px #618EA5） ---------- */
.login-version {
  margin: 0;
  font-family: var(--font-mono);
  font-size: 12px;
  letter-spacing: 0.06em;
  color: var(--color-text-muted); /* #618EA5 */
}

/* ============================================================
 * 响应式
 * ============================================================ */
/* <1024px：品牌折叠为顶部 Banner，表单全宽主导 */
@media (max-width: 1023px) {
  .login-layout {
    grid-template-columns: 1fr;
  }
  .login-brand {
    padding: var(--space-6);
    gap: 0;
    align-items: center;
  }
  .login-brand__banner {
    display: flex;
    align-items: center;
    gap: var(--space-2);
  }
  .login-brand__name {
    font-family: var(--font-display);
    font-size: var(--text-h2);
    font-weight: 600;
    color: var(--color-text-secondary);
  }
  .login-brand__body {
    display: none; /* 桌面叙事区折叠隐藏 */
  }
  .login-form-panel {
    padding: var(--space-8) var(--space-6) var(--space-6);
    max-width: none;
    box-shadow: none;
    align-items: center;
  }
  .login-version {
    align-self: center;
  }
}

/* 375px：无横向滚动，卡片级留白收紧 */
@media (max-width: 375px) {
  .login-page {
    overflow-x: hidden;
  }
  .login-brand {
    padding: var(--space-4);
  }
  .login-form-panel {
    padding: var(--space-6) var(--space-4);
  }
}

/* 入场动效：品牌与表单按 80ms 级联淡入上移，总时长 ≤500ms */
@media (prefers-reduced-motion: no-preference) {
  .login-brand__banner,
  .login-brand__body,
  .login-form-panel {
    animation: fade-up var(--dur-slow) var(--ease-out) both;
  }
  .login-brand__body {
    animation-delay: 0.08s;
  }
  .login-form-panel {
    animation-delay: 0.16s;
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
