import { describe, expect, it, vi } from 'vitest'
import JSZip from 'jszip'
import {
  createUploadSelection,
  prepareUploadFile
} from './uploadArchive'

const options = {
  allowedExtensions: ['mp4', 'mov', 'webm'],
  maxBytes: 500 * 1024 * 1024
}

const makeFile = (name, content = 'video', relativePath = '') => {
  const file = new File([content], name, { type: 'video/mp4', lastModified: 1 })
  if (relativePath) Object.defineProperty(file, 'webkitRelativePath', { value: relativePath })
  return file
}

describe('uploadArchive', () => {
  it('keeps a single regular file unchanged', async () => {
    const file = makeFile('campus.mp4')
    const selection = createUploadSelection([file], options)

    expect(selection.requiresArchive).toBe(false)
    await expect(prepareUploadFile(selection, {
      archiveBaseName: 'submission',
      maxBytes: options.maxBytes
    })).resolves.toBe(file)
  })

  it('packages multiple files and avoids duplicate archive paths', async () => {
    const selection = createUploadSelection([
      makeFile('clip.mp4', 'one'),
      makeFile('clip.mp4', 'two')
    ], options)
    const progress = vi.fn()
    const archive = await prepareUploadFile(selection, {
      archiveBaseName: 'submission',
      maxBytes: options.maxBytes,
      onProgress: progress
    })
    const zip = await JSZip.loadAsync(archive)

    expect(archive.name).toMatch(/^submission-\d{8}T\d{6}\.zip$/)
    expect(Object.keys(zip.files)).toEqual(['clip.mp4', 'clip (2).mp4'])
    expect(progress).toHaveBeenCalledWith(100)
  })

  it('preserves folder paths and archives a folder containing one file', async () => {
    const selection = createUploadSelection([
      makeFile('clip.mp4', 'video', 'campaign/raw/clip.mp4')
    ], options)
    const archive = await prepareUploadFile(selection, {
      archiveBaseName: 'submission',
      maxBytes: options.maxBytes
    })
    const zip = await JSZip.loadAsync(archive)

    expect(selection.requiresArchive).toBe(true)
    expect(zip.file('campaign/raw/clip.mp4')).not.toBeNull()
  })

  it('rejects unsupported files and unsafe folder paths', () => {
    expect(() => createUploadSelection([makeFile('notes.txt')], options))
      .toThrow('包含不支持的文件格式：notes.txt')
    expect(() => createUploadSelection([
      makeFile('clip.mp4', 'video', 'campaign/../clip.mp4')
    ], options)).toThrow('文件夹中包含不合法的文件路径')
  })

  it('enforces entry count and total selection size', () => {
    const tooMany = Array.from({ length: 501 }, (_, index) => makeFile(`${index}.mp4`))
    expect(() => createUploadSelection(tooMany, options)).toThrow('一次最多选择500个文件')

    expect(() => createUploadSelection([makeFile('large.mp4', '12345')], {
      ...options,
      maxBytes: 4
    })).toThrow('所选文件总大小超出限制')
  })
})
