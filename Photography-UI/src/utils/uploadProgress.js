export const getUploadPercentage = event => {
  if (!event) return null

  const loaded = Number(event.loaded)
  const total = Number(event.total)
  const progress = Number(event.progress)

  let ratio = null
  if (Number.isFinite(loaded) && Number.isFinite(total) && total > 0) {
    ratio = loaded / total
  } else if (Number.isFinite(progress)) {
    ratio = progress
  }

  if (!Number.isFinite(ratio)) return null
  return Math.min(100, Math.max(0, Math.round(ratio * 100)))
}
