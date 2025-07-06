import { useAuthStore } from '@/stores/authStore'
import { authAPI } from '@/services/auth'

export const usePermission = () => {
  const { permissions, roles, userInfo } = useAuthStore()

  const hasPermission = (permission: string | string[]): boolean => {
    if (!permissions || permissions.length === 0) return false
    
    if (typeof permission === 'string') {
      return permissions.includes(permission)
    }
    
    if (Array.isArray(permission)) {
      return permission.some(p => permissions.includes(p))
    }
    
    return false
  }

  const hasRole = (role: string | string[]): boolean => {
    if (!roles || roles.length === 0) return false
    
    if (typeof role === 'string') {
      return roles.includes(role)
    }
    
    if (Array.isArray(role)) {
      return role.some(r => roles.includes(r))
    }
    
    return false
  }

  const hasAllPermissions = (permissions: string[]): boolean => {
    return permissions.every(permission => hasPermission(permission))
  }

  const hasAllRoles = (roles: string[]): boolean => {
    return roles.every(role => hasRole(role))
  }

  const hasAnyPermission = (permissions: string[]): boolean => {
    return permissions.some(permission => hasPermission(permission))
  }

  const hasAnyRole = (roles: string[]): boolean => {
    return roles.some(role => hasRole(role))
  }

  const checkPermission = async (permission: string): Promise<boolean> => {
    try {
      const response = await authAPI.checkPermission(permission)
      return response.code === 200 ? response.data : false
    } catch (error) {
      return false
    }
  }

  const isAdmin = (): boolean => {
    return hasRole('admin') || hasRole('ADMIN') || hasRole('administrator')
  }

  const isSuperAdmin = (): boolean => {
    return hasRole('super_admin') || hasRole('SUPER_ADMIN')
  }

  const canAccess = (requiredPermissions?: string | string[], requiredRoles?: string | string[]): boolean => {
    if (isSuperAdmin()) return true
    
    let hasRequiredPermissions = true
    let hasRequiredRoles = true
    
    if (requiredPermissions) {
      hasRequiredPermissions = hasPermission(requiredPermissions)
    }
    
    if (requiredRoles) {
      hasRequiredRoles = hasRole(requiredRoles)
    }
    
    return hasRequiredPermissions && hasRequiredRoles
  }

  const getPermissionList = (): string[] => {
    return permissions || []
  }

  const getRoleList = (): string[] => {
    return roles || []
  }

  const getUserPermissions = async (): Promise<string[]> => {
    try {
      const response = await authAPI.getUserPermissions()
      return response.code === 200 ? response.data : []
    } catch (error) {
      return []
    }
  }

  return {
    hasPermission,
    hasRole,
    hasAllPermissions,
    hasAllRoles,
    hasAnyPermission,
    hasAnyRole,
    checkPermission,
    isAdmin,
    isSuperAdmin,
    canAccess,
    getPermissionList,
    getRoleList,
    getUserPermissions,
    permissions,
    roles,
    userInfo
  }
}

export default usePermission