import JSZip from 'jszip'

export const MAX_UPLOAD_ENTRIES = 500

const getExtension = filename => {
  const match = /\.([^.\/]+)$/.exec(filename)
  return match ? match[1].toLowerCase() : ''
}

const validateRelativePath = path => {
  if (!path || path.length > 1024 || path.includes('\0') || path.startsWith('/') || /^[a-z]:/i.test(path)) {
    throw new Error('文件路径不合法，请重新选择')
  }

  const segments = path.split('/')
  if (segments.some(segment => !segment || segment === '.' || segment === '..' || segment.length > 255)) {
    throw new Error('文件夹中包含不合法的文件路径')
  }
}

const getRelativePath = file => {
  const path = (file.webkitRelativePath || file.name || '').replaceAll('\\', '/')
  validateRelativePath(path)
  return path
}

const makeUniquePath = (path, usedPaths) => {
  const normalized = path.toLowerCase()
  if (!usedPaths.has(normalized)) {
    usedPaths.add(normalized)
    return path
  }

  const slashIndex = path.lastIndexOf('/')
  const directory = slashIndex >= 0 ? path.slice(0, slashIndex + 1) : ''
  const filename = slashIndex >= 0 ? path.slice(slashIndex + 1) : path
  const dotIndex = filename.lastIndexOf('.')
  const stem = dotIndex > 0 ? filename.slice(0, dotIndex) : filename
  const suffix = dotIndex > 0 ? filename.slice(dotIndex) : ''

  let index = 2
  let candidate
  do {
    candidate = `${directory}${stem} (${index})${suffix}`
    index += 1
  } while (usedPaths.has(candidate.toLowerCase()))

  usedPaths.add(candidate.toLowerCase())
  return candidate
}

export const createUploadSelection = (files, options) => {
  const {
    allowedExtensions,
    maxBytes,
    maxEntries = MAX_UPLOAD_ENTRIES,
    emptyFileMessage = '不能上传空文件',
    unsupportedMessage = '包含不支持的文件格式',
    sizeMessage = '所选文件总大小超出限制'
  } = options
  const selectedFiles = Array.from(files || [])

  if (selectedFiles.length > maxEntries) {
    throw new Error(`一次最多选择${maxEntries}个文件`)
  }

  const allowed = new Set(Array.from(allowedExtensions, extension => extension.toLowerCase()))
  const usedPaths = new Set()
  let totalBytes = 0

  const entries = selectedFiles.map((file, index) => {
    if (!file || !file.name || file.size <= 0) {
      throw new Error(emptyFileMessage)
    }

    const path = getRelativePath(file)
    const extension = getExtension(path)
    if (!allowed.has(extension)) {
      throw new Error(`${unsupportedMessage}：${path}`)
    }

    totalBytes += file.size
    if (!Number.isSafeInteger(totalBytes) || totalBytes > maxBytes) {
      throw new Error(sizeMessage)
    }

    return {
      id: `${file.name}-${file.size}-${file.lastModified || 0}-${index}`,
      file,
      path,
      archivePath: makeUniquePath(path, usedPaths),
      extension
    }
  })

  return {
    entries,
    totalBytes,
    requiresArchive: entries.length > 1 || entries.some(entry => Boolean(entry.file.webkitRelativePath))
  }
}

export const prepareUploadFile = async (selection, options) => {
  const { archiveBaseName, maxBytes, onProgress = () => {} } = options
  if (!selection?.entries?.length) return null
  if (!selection.requiresArchive) return selection.entries[0].file

  const zip = new JSZip()
  selection.entries.forEach(entry => {
    zip.file(entry.archivePath, entry.file, {
      binary: true,
      compression: 'STORE',
      createFolders: false
    })
  })

  onProgress(0)
  const blob = await zip.generateAsync(
    {
      type: 'blob',
      mimeType: 'application/zip',
      compression: 'STORE',
      platform: 'UNIX'
    },
    metadata => onProgress(Math.min(100, Math.max(0, Math.round(metadata.percent))))
  )

  if (blob.size > maxBytes) {
    throw new Error('打包后的压缩包超出大小限制，请移除部分文件后重试')
  }

  const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace(/\.\d{3}Z$/, '')
  return new File([blob], `${archiveBaseName}-${timestamp}.zip`, {
    type: 'application/zip',
    lastModified: Date.now()
  })
}

export const formatUploadSize = bytes => {
  if (!bytes) return '0 MB'
  const megabytes = bytes / 1024 / 1024
  return megabytes >= 10 ? `${Math.round(megabytes)} MB` : `${megabytes.toFixed(1)} MB`
}
