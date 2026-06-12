const KNOWN_UPLOAD_IMAGE_DIRS = new Set(['avatars', 'equipment', 'returns', 'site'])

const normalizePath = (url) => {
  return String(url || '')
    .trim()
    .replace(/\\/g, '/')
    .replace(/\/uploads\/uploads\//g, '/uploads/')
}

const getFileName = (path) => {
  const cleanPath = path.split('?')[0].split('#')[0]
  return cleanPath.substring(cleanPath.lastIndexOf('/') + 1)
}

export const getUploadedImageUrl = (url) => {
  let cleanUrl = normalizePath(url)
  if (!cleanUrl) return ''
  if (cleanUrl.startsWith('data:') || cleanUrl.startsWith('blob:')) {
    return cleanUrl
  }
  if (/^https?:\/\//i.test(cleanUrl)) {
    try {
      const parsedUrl = new URL(cleanUrl)
      if (typeof window === 'undefined' || parsedUrl.origin !== window.location.origin) {
        return cleanUrl
      }
      cleanUrl = `${parsedUrl.pathname}${parsedUrl.search}${parsedUrl.hash}`
    } catch (error) {
      return cleanUrl
    }
  }
  if (cleanUrl.startsWith('/api/images/') || cleanUrl.startsWith('/api/uploads/')) {
    return cleanUrl
  }

  const uploadMatch = cleanUrl.match(/(?:^|\/)uploads\/([^/]+)\/([^?#]+)(.*)$/)
  if (uploadMatch) {
    const dir = uploadMatch[1]
    const fileName = getFileName(uploadMatch[2])
    const suffix = uploadMatch[3] || ''

    if (KNOWN_UPLOAD_IMAGE_DIRS.has(dir)) {
      return `/api/images/${dir}/${fileName}${suffix}`
    }

    return `/api/uploads/${dir}/${fileName}${suffix}`
  }

  if (cleanUrl.startsWith('/uploads/')) {
    return `/api${cleanUrl}`
  }

  return cleanUrl
}

export const getSiteImageUrl = (url) => getUploadedImageUrl(url)
