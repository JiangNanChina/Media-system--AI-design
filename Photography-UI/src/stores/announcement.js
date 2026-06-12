import { defineStore } from 'pinia'

// 仅在当前浏览器标签页的运行时内存中保存，页面刷新清空，但路由跳转不丢失
export const useAnnouncementStore = defineStore('announcement', {
  state: () => ({
    readIdsInRuntime: [] // 当前运行时内存中的已读公告ID
  }),
  actions: {
    markRead(id) {
      if (id == null) return
      if (!this.readIdsInRuntime.includes(id)) {
        this.readIdsInRuntime.push(id)
      }
    },
    markManyRead(ids) {
      if (!Array.isArray(ids)) return
      ids.forEach(id => this.markRead(id))
    },
    isRead(id) {
      return this.readIdsInRuntime.includes(id)
    },
    clearRuntime() {
      this.readIdsInRuntime = []
    }
  }
})


