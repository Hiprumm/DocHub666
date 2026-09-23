<script setup lang="ts">
/**
 * 原型侧登录页 Demo —— 暖棕「纸质笔记本」视觉方向验证
 * 本文件为原型：所有 token 以 scoped 作用域内联定义，不污染全局 tokens.css。
 * 仅供方向评审，未接入真实登录逻辑。
 */
import { ref } from 'vue'

const username = ref('')
const password = ref('')
</script>

<template>
  <div class="paper-login">
    <!-- 左：书籍扉页氛围区（内容主角） -->
    <aside class="marker">
      <p class="marker-kicker">DocHub</p>
      <h1 class="marker-title">把知识，<br>写进纸页里。</h1>
      <p class="marker-blurb">
        一个安静的企业文档平台。检索、阅读、协作，都像翻开一本装帧考究的书——
        工具退到背景，让人回到内容本身。
      </p>
      <div class="marker-rule" />
      <p class="marker-note">为长时间阅读与编辑的知识工作者而作</p>
    </aside>

    <!-- 右：书页式登录表单 -->
    <section class="form-wrap">
      <form class="form" @submit.prevent>
        <p class="form-kicker">登录到你的工作空间</p>
        <h2 class="form-title">欢迎回来</h2>

        <label class="field">
          <span class="field-lbl">账号</span>
          <input
            v-model="username"
            class="field-ip"
            type="text"
            name="username"
            autocomplete="username"
            placeholder="你的用户名"
          >
        </label>

        <label class="field">
          <span class="field-lbl">密码</span>
          <input
            v-model="password"
            class="field-ip"
            type="password"
            name="password"
            autocomplete="current-password"
            placeholder="输入密码"
          >
        </label>

        <button class="submit" type="button">进入工作空间</button>

        <p class="form-foot">需要帮助？联系你的系统管理员。</p>
      </form>
    </section>
  </div>
</template>

<style scoped>
/* ========= 暖棕纸质 token（仅本组件作用域） ========= */
.paper-login {
  /* 色板 */
  --paper-ink: #2e2118; /* 最深墨棕：正文 */
  --paper-strong: #492d22; /* 暖棕：主强调 */
  --paper-brown: #7a5c43; /* 棕褐：次级 */
  --paper-bg: #e6cfb6; /* 书脊米底 */
  --paper-light: #f0e8dd; /* 浅纸：表单面 */
  --paper-line: #c9b79f; /* 细发线 */
  --paper-fade: rgba(46, 33, 24, 0.55); /* 淡墨 */

  /* 排版 */
  --serif: 'Source Serif 4', 'Noto Serif SC', Georgia, 'Songti SC', serif;

  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 0;
  min-height: 100vh;
  background: var(--paper-bg);
  color: var(--paper-ink);
  font-family: var(--serif);
  -webkit-font-smoothing: antialiased;
}

/* ---------- 左：扉页氛围 ---------- */
.marker {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-6, 24px);
  padding: clamp(40px, 8vw, 96px);
  background: var(--paper-strong);
  color: var(--paper-light);
}

.marker-kicker {
  margin: 0;
  font-size: 15px;
  letter-spacing: 0.18em;
  opacity: 0.82;
}

.marker-title {
  margin: 0;
  font-family: var(--serif);
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
  background: var(--paper-light);
  opacity: 0.7;
}

.marker-note {
  margin: 0;
  font-size: 14px;
  opacity: 0.6;
}

/* ---------- 右：书页表单 ---------- */
.form-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(32px, 6vw, 72px);
  /* 模拟纸面的柔和暖光，非冷灰阴影 */
  background:
    radial-gradient(120% 90% at 70% -10%, rgba(255, 255, 255, 0.55), transparent 55%),
    var(--paper-light);
}

.form {
  width: 100%;
  max-width: 380px;
}

.form-kicker {
  margin: 0 0 10px;
  font-size: 14px;
  letter-spacing: 0.12em;
  color: var(--paper-brown);
}

.form-title {
  margin: 0 0 36px;
  font-family: var(--serif);
  font-weight: 500;
  font-size: 30px;
  line-height: 1.3;
}

.field {
  display: block;
  margin-bottom: 22px;
}

.field-lbl {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--paper-strong);
}

.field-ip {
  width: 100%;
  padding: 12px 14px;
  font-family: inherit;
  font-size: 16px;
  color: var(--paper-ink);
  background: transparent;
  border: 1px solid var(--paper-line);
  border-radius: 2px;
  transition: border-color 180ms ease, box-shadow 180ms ease;
}

.field-ip::placeholder {
  color: var(--paper-fade);
}

.field-ip:focus {
  outline: none;
  border-color: var(--paper-strong);
  box-shadow: 0 0 0 3px rgba(73, 45, 34, 0.12);
}

.submit {
  width: 100%;
  margin-top: 8px;
  padding: 13px 16px;
  font-family: inherit;
  font-size: 16px;
  color: var(--paper-light);
  background: var(--paper-strong);
  border: none;
  border-radius: 2px;
  cursor: pointer;
  transition: background 180ms ease;
}

.submit:hover {
  background: #3a241a;
}

.form-foot {
  margin: 26px 0 0;
  font-size: 13px;
  text-align: center;
  color: var(--paper-brown);
}

/* ---------- 窄屏 ---------- */
@media (max-width: 760px) {
  .paper-login {
    grid-template-columns: 1fr;
  }
  .marker {
    padding: 44px 28px;
  }
}
</style>