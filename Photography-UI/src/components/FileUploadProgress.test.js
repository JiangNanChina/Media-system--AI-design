import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import FileUploadProgress from './FileUploadProgress.vue'

const ElProgressStub = {
  props: ['percentage', 'status'],
  template: '<div class="progress-stub" :data-percentage="percentage" :data-status="status" />'
}

const mountProgress = props => mount(FileUploadProgress, {
  props,
  global: {
    stubs: { ElProgress: ElProgressStub }
  }
})

describe('FileUploadProgress', () => {
  it('shows live upload progress with a stable numeric percentage', () => {
    const wrapper = mountProgress({ percentage: 42, subject: '视频' })

    expect(wrapper.text()).toContain('视频上传中')
    expect(wrapper.text()).toContain('42%')
    expect(wrapper.get('.progress-stub').attributes('data-percentage')).toBe('42')
    expect(wrapper.attributes('aria-label')).toBe('视频上传中，42%')
  })

  it('distinguishes server processing and retryable error states', async () => {
    const wrapper = mountProgress({ percentage: 100, subject: '作品', status: 'processing' })

    expect(wrapper.text()).toContain('作品上传完成')
    expect(wrapper.text()).toContain('等待服务器确认')

    await wrapper.setProps({ percentage: 68, status: 'error' })
    expect(wrapper.text()).toContain('作品上传中断')
    expect(wrapper.text()).toContain('可重新提交')
    expect(wrapper.get('.progress-stub').attributes('data-status')).toBe('exception')
  })
})
