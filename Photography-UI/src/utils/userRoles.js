const LEGACY_ROLE_ALIASES = {
  ADMIN: 'SUPER_ADMIN'
}

const ROLE_RANK = {
  MEMBER: 1,
  MINISTER: 2,
  ADVISOR: 2,
  DIRECTOR: 3,
  SUPER_ADMIN: 4
}

const ROLE_SORT_ORDER = ['SUPER_ADMIN', 'DIRECTOR', 'MINISTER', 'ADVISOR', 'MEMBER']

const ROLE_GRANTS = {
  MEMBER: ['MEMBER'],
  MINISTER: ['MINISTER'],
  ADVISOR: ['ADVISOR'],
  DIRECTOR: ['DIRECTOR', 'MINISTER'],
  SUPER_ADMIN: ['SUPER_ADMIN', 'DIRECTOR', 'MINISTER']
}

export const ROLE_OPTIONS = [
  { label: '部员', value: 'MEMBER', tagType: 'primary' },
  { label: '部长', value: 'MINISTER', tagType: 'success' },
  { label: '指导老师', value: 'ADVISOR', tagType: 'info' },
  { label: '主任', value: 'DIRECTOR', tagType: 'warning' },
  { label: '系统超级管理员', value: 'SUPER_ADMIN', tagType: 'danger' }
]

const roleDefinition = new Map(ROLE_OPTIONS.map(option => [option.value, option]))

export const normalizeRole = role => {
  const normalized = LEGACY_ROLE_ALIASES[String(role || '').trim().toUpperCase()] || String(role || '').trim().toUpperCase()
  return roleDefinition.has(normalized) ? normalized : ''
}

export const sortRoles = roles => [...roles].sort((left, right) => {
  const rankDifference = (ROLE_RANK[right] || 0) - (ROLE_RANK[left] || 0)
  if (rankDifference !== 0) return rankDifference
  return ROLE_SORT_ORDER.indexOf(left) - ROLE_SORT_ORDER.indexOf(right)
})

export const normalizeRoles = (roles, fallbackRole = '') => {
  const suppliedRoles = Array.isArray(roles) ? roles : (roles ? [roles] : [])
  const source = suppliedRoles.length > 0 ? suppliedRoles : (fallbackRole ? [fallbackRole] : [])
  const uniqueRoles = [...new Set(source.map(normalizeRole).filter(Boolean))]
  return sortRoles(uniqueRoles)
}

export const getUserRoles = user => normalizeRoles(user?.roles, user?.role || user?.highestRole)

export const getHighestRoles = roles => {
  const normalizedRoles = normalizeRoles(roles)
  if (normalizedRoles.length === 0) return []
  const highestRank = ROLE_RANK[normalizedRoles[0]]
  return normalizedRoles.filter(role => ROLE_RANK[role] === highestRank)
}

export const getHighestRole = roles => getHighestRoles(roles)[0] || ''

export const getGrantedRoles = roles => {
  const granted = getHighestRoles(roles).flatMap(role => ROLE_GRANTS[role] || [role])
  return sortRoles([...new Set(granted)])
}

export const hasAnyGrantedRole = (roles, requiredRoles) => {
  const grantedRoles = new Set(getGrantedRoles(roles))
  return normalizeRoles(requiredRoles).some(role => grantedRoles.has(role))
}

export const roleLabel = role => roleDefinition.get(normalizeRole(role))?.label || role || ''

export const roleTagType = role => roleDefinition.get(normalizeRole(role))?.tagType || 'info'

export const sameRoles = (left, right) => {
  const normalizedLeft = normalizeRoles(left)
  const normalizedRight = normalizeRoles(right)
  return normalizedLeft.length === normalizedRight.length &&
    normalizedLeft.every((role, index) => role === normalizedRight[index])
}
