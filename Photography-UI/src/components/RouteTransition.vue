<template>
  <Transition name="route-loader-fade">
    <div
      v-if="state.active.value"
      class="route-transition-overlay"
      role="status"
      aria-live="polite"
      aria-label="页面加载中"
      data-testid="route-transition"
    >
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

        <div class="route-transition-copy">
          <span>LOADING FRAME</span>
          <strong>正在切换画面</strong>
          <small>{{ state.title.value }} / PLEASE STAND BY</small>
        </div>

        <div class="route-transition-progress" aria-hidden="true">
          <span></span>
        </div>
      </div>

      <footer class="route-transition-footer" aria-hidden="true">
        <span>CHANNEL / MEMBER SYSTEM</span>
        <span><i></i> SIGNAL LOCKED</span>
      </footer>
    </div>
  </Transition>
</template>

<script setup>
import { routeTransitionState as state } from '@/utils/routeTransition'
</script>

<style scoped>
.route-transition-overlay {
  --loader-ink: #11100e;
  --loader-paper: #f4f3ef;
  --loader-red: #d84a36;
  --loader-yellow: #e4b84e;
  --loader-mono: "Cascadia Mono", "SFMono-Regular", Consolas, monospace;
  position: fixed !important;
  z-index: 2147483000 !important;
  inset: 0;
  display: grid;
  grid-template-rows: auto 1fr auto;
  min-width: 320px;
  min-height: 100dvh;
  padding: clamp(22px, 4vw, 54px);
  overflow: hidden;
  color: var(--loader-paper);
  background: var(--loader-ink);
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

.route-transition-overlay::before {
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
  font-family: var(--loader-mono);
  font-size: clamp(8px, 0.8vw, 10px);
  font-weight: 700;
}

.route-transition-meta {
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.18);
}

.route-transition-meta span:first-child,
.route-transition-footer span:last-child {
  color: var(--loader-yellow);
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
  background: repeating-linear-gradient(90deg, var(--loader-paper) 0 12px, transparent 12px 23px);
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
  border-color: var(--loader-yellow);
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
  background: var(--loader-red);
  opacity: 0.82;
  animation: film-scan 760ms cubic-bezier(0.65, 0, 0.35, 1) infinite;
}

.film-rec,
.film-timecode {
  position: absolute;
  font-family: var(--loader-mono);
  font-size: 8px;
  font-weight: 700;
}

.film-rec {
  top: 17px;
  left: 45px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--loader-red);
}

.film-rec b,
.route-transition-footer i {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: var(--loader-red);
  border-radius: 50%;
  box-shadow: 0 0 0 3px rgba(216, 74, 54, 0.18);
}

.film-timecode {
  right: 44px;
  bottom: 17px;
  color: var(--loader-yellow);
  font-variant-numeric: tabular-nums;
}

.route-transition-copy {
  display: grid;
  gap: 7px;
  justify-items: center;
  text-align: center;
}

.route-transition-copy > span,
.route-transition-copy small {
  font-family: var(--loader-mono);
  font-weight: 700;
  text-transform: uppercase;
}

.route-transition-copy > span {
  color: var(--loader-red);
  font-size: 9px;
}

.route-transition-copy strong {
  color: var(--loader-paper);
  font-family: "Arial Black", "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: clamp(25px, 4vw, 38px);
  line-height: 1.1;
  text-wrap: balance;
}

.route-transition-copy small {
  color: rgba(255, 255, 255, 0.42);
  font-size: 8px;
  text-wrap: pretty;
}

.route-transition-progress {
  width: min(390px, 86vw);
  height: 4px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.14);
}

.route-transition-progress span {
  display: block;
  width: 42%;
  height: 100%;
  background: var(--loader-yellow);
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
  transform: translateY(-12px);
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

  .route-transition-progress {
    width: min(330px, 88vw);
  }
}

@media (prefers-reduced-motion: reduce) {
  .film-track,
  .film-scan,
  .route-transition-progress span {
    animation: none !important;
  }

  .film-scan {
    left: 41%;
  }

  .route-transition-progress span {
    width: 62%;
  }

  .route-loader-fade-leave-active {
    transition-duration: 0.01ms !important;
  }
}
</style>
