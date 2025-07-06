import React from 'react'
import { usePermission } from '@/hooks'

interface PermissionWrapperProps {
  permission?: string | string[]
  role?: string | string[]
  fallback?: React.ReactNode
  children: React.ReactNode
  requireAll?: boolean
}

const PermissionWrapper: React.FC<PermissionWrapperProps> = ({
  permission,
  role,
  fallback = null,
  children,
  requireAll = false
}) => {
  const { hasPermission, hasRole, isAdmin } = usePermission()

  // 管理员拥有所有权限
  if (isAdmin()) {
    return <>{children}</>
  }

  let hasRequiredPermission = true
  let hasRequiredRole = true

  // 检查权限
  if (permission) {
    if (Array.isArray(permission)) {
      if (requireAll) {
        hasRequiredPermission = permission.every(p => hasPermission(p))
      } else {
        hasRequiredPermission = permission.some(p => hasPermission(p))
      }
    } else {
      hasRequiredPermission = hasPermission(permission)
    }
  }

  // 检查角色
  if (role) {
    if (Array.isArray(role)) {
      if (requireAll) {
        hasRequiredRole = role.every(r => hasRole(r))
      } else {
        hasRequiredRole = role.some(r => hasRole(r))
      }
    } else {
      hasRequiredRole = hasRole(role)
    }
  }

  // 如果同时指定了权限和角色，两者都必须满足（AND关系）
  const hasAccess = hasRequiredPermission && hasRequiredRole

  return hasAccess ? <>{children}</> : <>{fallback}</>
}

export default PermissionWrapper