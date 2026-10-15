<template>
  <Transition name="route-loader-fade">
    <div
      v-if="state.active.value"
      class="route-transition-overlay"
      :class="`route-transition-overlay--${state.theme.value}`"
      :data-transition-theme="state.theme.value"
      role="status"
      aria-live="polite"
      :aria-label="`正在加载${state.title.value}`"
      data-testid="route-transition"
    >
      <template v-if="state.theme.value === 'archive'">
        <div class="route-transition-grid" aria-hidden="true"></div>

        <header class="route-transition-meta" aria-hidden="true">
          <span>MEDIA ARCHIVE / TRANSFER</span>
          <span>FRAME {{ state.code.value }}</span>
        </header>

        <div class="route-transition-stage">
          <div class="film-gate" aria-hidden="true">
            <div class="film-track film-track-top"></div>
            <div class="film-aperture">
              <i></i><i></i><i></i><i></i>
              <span class="film-scan"></span>
              <span class="film-rec"><b></b> REC</span>
              <span class="film-timecode">00:00:00:{{ state.code.value }}</span>
            </div>
            <div class="film-track film-track-bottom"></div>
          </div>

          <div class="archive-transition-copy">
            <span>LOADING FRAME</span>
            <strong>正在切换画面</strong>
            <small>{{ state.title.value }} / PLEASE STAND BY</small>
          </div>

          <div class="archive-transition-progress" aria-hidden="true">
            <span></span>
          </div>
        </div>

        <footer class="route-transition-footer" aria-hidden="true">
          <span>CHANNEL / MEMBER SYSTEM</span>
          <span><i></i> SIGNAL LOCKED</span>
        </footer>
      </template>

      <section v-else class="route-transition-panel">
        <div class="route-transition-mark" aria-hidden="true">
          <span class="loader-ring loader-ring-outer"></span>
          <span class="loader-ring loader-ring-inner"></span>
          <span class="loader-orbit-dot"></span>
          <span class="loader-icon-surface">
            <el-icon><Camera /></el-icon>
          </span>
        </div>

        <div class="admin-transition-copy">
          <strong>{{ state.title.value }}</strong>
          <span>页面加载中，请稍候</span>
        </div>

        <div class="admin-transition-progress" aria-hidden="true">
          <span></span>
        </div>

        <div class="admin-transition-status" aria-hidden="true">
          <i></i>
          <span>正在同步页面内容</span>
        </div>
      </section>
    </div>
  </Transition>
</template>

<script setup>
import { routeTransitionState as state } from '@/utils/routeTransition'
</script>

<style scoped>
.route-transition-overlay--admin {
  --loader-primary: var(--color-primary-500, #159ee5);
  --loader-primary-strong: var(--color-primary-600, #087fc4);
  --loader-success: var(--color-success, #35c7a3);
  --loader-ink: var(--color-text-primary, #12384f);
  --loader-muted: var(--color-text-secondary, #4f6d82);
  position: fixed !important;
  z-index: 2147483000 !important;
  inset: 0;
  display: grid;
  min-width: 320px;
  min-height: 100dvh;
  place-items: center;
  padding: 24px;
  overflow: hidden;
  color: var(--loader-ink);
  background: rgba(240, 249, 253, 0.86);
  backdrop-filter: blur(18px) saturate(1.06);
  -webkit-backdrop-filter: blur(18px) saturate(1.06);
  font-family: var(--font-family-primary, Inter, "PingFang SC", "Microsoft YaHei", sans-serif);
  cursor: wait;
}

.route-transition-overlay--admin::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(rgba(37, 184, 242, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(37, 184, 242, 0.035) 1px, transparent 1px);
  background-size: 48px 48px;
  -webkit-mask-image: radial-gradient(circle at center, #000, transparent 72%);
  mask-image: radial-gradient(circle at center, #000, transparent 72%);
}

.route-transition-panel {
  position: relative;
  display: grid;
  justify-items: center;
  width: min(360px, calc(100vw - 40px));
  padding: 32px 32px 24px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(247, 253, 255, 0.88));
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 22px;
  box-shadow:
    0 24px 70px rgba(21, 122, 177, 0.14),
    inset 0 1px 0 rgba(255, 255, 255, 0.92);
}

.route-transition-panel::before {
  content: "";
  position: absolute;
  inset: 0;
  border: 1px solid rgba(135, 185, 214, 0.2);
  border-radius: inherit;
  pointer-events: none;
}

.route-transition-mark {
  position: relative;
  width: 88px;
  height: 88px;
  margin-bottom: 20px;
}

.loader-ring,
.loader-orbit-dot,
.loader-icon-surface {
  position: absolute;
  border-radius: 50%;
}

.loader-ring {
  inset: 0;
  border: 2px solid rgba(37, 184, 242, 0.12);
}

.loader-ring-outer {
  border-top-color: var(--loader-primary);
  border-right-color: rgba(53, 199, 163, 0.68);
  animation: loader-spin 900ms linear infinite;
}

.loader-ring-inner {
  inset: 8px;
  border-width: 1px;
  border-bottom-color: var(--loader-success);
  animation: loader-spin-reverse 1.35s linear infinite;
}

.loader-orbit-dot {
  top: 1px;
  left: 50%;
  width: 7px;
  height: 7px;
  background: var(--loader-primary);
  box-shadow: 0 0 0 5px rgba(37, 184, 242, 0.12);
  transform: translateX(-50%);
}

.loader-icon-surface {
  inset: 17px;
  display: grid;
  place-items: center;
  color: var(--loader-primary-strong);
  background: linear-gradient(145deg, #f7fdff, #e0f5ff 58%, #ecfff9);
  border: 1px solid rgba(37, 184, 242, 0.22);
  box-shadow: 0 10px 24px rgba(21, 122, 177, 0.12);
}

.loader-icon-surface .el-icon {
  font-size: 25px;
}

.admin-transition-copy {
  display: grid;
  gap: 7px;
  justify-items: center;
  text-align: center;
}

.admin-transition-copy strong {
  max-width: 100%;
  color: var(--loader-ink);
  font-size: 20px;
  font-weight: 800;
  line-height: 1.3;
  letter-spacing: 0;
  overflow-wrap: anywhere;
  text-wrap: balance;
}

.admin-transition-copy > span {
  color: var(--loader-muted);
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0;
}

.admin-transition-progress {
  width: 100%;
  height: 4px;
  margin-top: 22px;
  overflow: hidden;
  background: rgba(135, 185, 214, 0.2);
  border-radius: 999px;
}

.admin-transition-progress span {
  display: block;
  width: 46%;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--loader-primary), var(--loader-success));
  box-shadow: 0 0 12px rgba(37, 184, 242, 0.28);
  animation: loader-progress 1.05s cubic-bezier(0.65, 0, 0.35, 1) infinite;
  will-change: transform;
}

.admin-transition-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 13px;
  color: #7691a4;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0;
}

.admin-transition-status i {
  width: 6px;
  height: 6px;
  background: var(--loader-success);
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(53, 199, 163, 0.12);
  animation: loader-status 1.25s ease-out infinite;
}

.route-transition-overlay--archive {
  --archive-ink: #11100e;
  --archive-paper: #f4f3ef;
  --archive-red: #d84a36;
  --archive-yellow: #e4b84e;
  --archive-mono: "Cascadia Mono", "SFMono-Regular", Consolas, monospace;
  position: fixed !important;
  z-index: 2147483000 !important;
  inset: 0;
  display: grid;
  grid-template-rows: auto 1fr auto;
  min-width: 320px;
  min-height: 100dvh;
  padding: clamp(22px, 4vw, 54px);
  overflow: hidden;
  color: var(--archive-paper);
  background: var(--archive-ink);
  font-family: "Noto Sans CJK SC", "Source Han Sans SC", "Microsoft YaHei", sans-serif;
  cursor: wait;
}

.route-transition-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(rgba(255, 255, 255, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.045) 1px, transparent 1px);
  background-size: 34px 34px;
  -webkit-mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.9), transparent 72%);
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.9), transparent 72%);
}

.route-transition-overlay--archive::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: min(68vw, 860px);
  aspect-ratio: 1;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 50%;
  box-shadow:
    0 0 0 9vw rgba(255, 255, 255, 0.018),
    0 0 0 18vw rgba(255, 255, 255, 0.012);
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.route-transition-meta,
.route-transition-footer {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  color: rgba(255, 255, 255, 0.48);
  font-family: var(--archive-mono);
  font-size: clamp(8px, 0.8vw, 10px);
  font-weight: 700;
}

.route-transition-meta {
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.18);
}

.route-transition-meta span:first-child,
.route-transition-footer span:last-child {
  color: var(--archive-yellow);
}

.route-transition-stage {
  position: relative;
  z-index: 2;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 24px;
  width: min(560px, 100%);
  margin: 0 auto;
}

.film-gate {
  position: relative;
  width: min(390px, 86vw);
  padding: 23px 0;
  overflow: hidden;
  background: #1d1c19;
  border: 1px solid rgba(255, 255, 255, 0.34);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.38);
}

.film-track {
  position: absolute;
  right: 0;
  left: 0;
  height: 12px;
  background: repeating-linear-gradient(90deg, var(--archive-paper) 0 12px, transparent 12px 23px);
  opacity: 0.88;
  animation: film-feed 540ms steps(4, end) infinite;
}

.film-track-top {
  top: 6px;
}

.film-track-bottom {
  bottom: 6px;
}

.film-aperture {
  position: relative;
  height: 146px;
  margin: 0 22px;
  overflow: hidden;
  background:
    linear-gradient(rgba(255, 255, 255, 0.075) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.075) 1px, transparent 1px),
    #151412;
  background-size: 28px 28px;
  border: 1px solid rgba(255, 255, 255, 0.24);
}

.film-aperture > i {
  position: absolute;
  width: 22px;
  height: 22px;
  border-color: var(--archive-yellow);
}

.film-aperture > i:nth-child(1) {
  top: 12px;
  left: 12px;
  border-top: 2px solid;
  border-left: 2px solid;
}

.film-aperture > i:nth-child(2) {
  top: 12px;
  right: 12px;
  border-top: 2px solid;
  border-right: 2px solid;
}

.film-aperture > i:nth-child(3) {
  bottom: 12px;
  left: 12px;
  border-bottom: 2px solid;
  border-left: 2px solid;
}

.film-aperture > i:nth-child(4) {
  right: 12px;
  bottom: 12px;
  border-right: 2px solid;
  border-bottom: 2px solid;
}

.film-scan {
  position: absolute;
  top: 0;
  bottom: 0;
  left: -22%;
  width: 18%;
  background: var(--archive-red);
  opacity: 0.82;
  animation: film-scan 760ms cubic-bezier(0.65, 0, 0.35, 1) infinite;
}

.film-rec,
.film-timecode {
  position: absolute;
  font-family: var(--archive-mono);
  font-size: 8px;
  font-weight: 700;
}

.film-rec {
  top: 17px;
  left: 45px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--archive-red);
}

.film-rec b,
.route-transition-footer i {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: var(--archive-red);
  border-radius: 50%;
  box-shadow: 0 0 0 3px rgba(216, 74, 54, 0.18);
}

.film-timecode {
  right: 44px;
  bottom: 17px;
  color: var(--archive-yellow);
  font-variant-numeric: tabular-nums;
}

.archive-transition-copy {
  display: grid;
  gap: 7px;
  justify-items: center;
  text-align: center;
}

.archive-transition-copy > span,
.archive-transition-copy small {
  font-family: var(--archive-mono);
  font-weight: 700;
  text-transform: uppercase;
}

.archive-transition-copy > span {
  color: var(--archive-red);
  font-size: 9px;
}

.archive-transition-copy strong {
  color: var(--archive-paper);
  font-family: "Arial Black", "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: clamp(25px, 4vw, 38px);
  line-height: 1.1;
  text-wrap: balance;
}

.archive-transition-copy small {
  color: rgba(255, 255, 255, 0.42);
  font-size: 8px;
  text-wrap: pretty;
}

.archive-transition-progress {
  width: min(390px, 86vw);
  height: 4px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.14);
}

.archive-transition-progress span {
  display: block;
  width: 42%;
  height: 100%;
  background: var(--archive-yellow);
  animation: film-progress 760ms cubic-bezier(0.65, 0, 0.35, 1) infinite;
}

.route-transition-footer {
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.18);
}

.route-transition-footer span:last-child {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.route-loader-fade-leave-active {
  transition: opacity 150ms ease-out, transform 150ms ease-out, filter 150ms ease-out;
}

.route-loader-fade-leave-to {
  opacity: 0;
  filter: blur(4px);
  transform: translateY(-8px);
}

.route-transition-overlay--archive.route-loader-fade-leave-to {
  transform: translateY(-12px);
}

@keyframes loader-spin {
  to { transform: rotate(360deg); }
}

@keyframes loader-spin-reverse {
  to { transform: rotate(-360deg); }
}

@keyframes loader-progress {
  0% { transform: translateX(-105%); }
  62%, 100% { transform: translateX(225%); }
}

@keyframes loader-status {
  0%, 100% { opacity: 0.55; }
  45% { opacity: 1; }
}

@keyframes film-feed {
  to { background-position-x: 23px; }
}

@keyframes film-scan {
  0% { left: -22%; }
  55%, 100% { left: 104%; }
}

@keyframes film-progress {
  0% { transform: translateX(-105%); }
  60%, 100% { transform: translateX(240%); }
}

@media (max-width: 520px) {
  .route-transition-overlay {
    padding: 20px;
  }

  .route-transition-panel {
    padding: 28px 24px 22px;
  }

  .route-transition-overlay--archive {
    padding: 18px 16px;
  }

  .route-transition-meta span:first-child,
  .route-transition-footer span:first-child {
    max-width: 62%;
  }

  .film-gate {
    width: min(330px, 88vw);
  }

  .film-aperture {
    height: 126px;
    margin-inline: 16px;
  }

  .archive-transition-progress {
    width: min(330px, 88vw);
  }
}

@media (prefers-reduced-motion: reduce) {
  .loader-ring,
  .admin-transition-progress span,
  .admin-transition-status i {
    animation: none !important;
  }

  .film-track,
  .film-scan,
  .archive-transition-progress span {
    animation: none !important;
  }

  .film-scan {
    left: 41%;
  }

  .archive-transition-progress span {
    width: 62%;
  }

  .admin-transition-progress span {
    width: 62%;
  }

  .route-loader-fade-leave-active {
    transition-duration: 0.01ms !important;
  }
}
</style>
