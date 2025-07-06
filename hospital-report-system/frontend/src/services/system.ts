import { http } from '@/utils/request'
import type { User, Role, Permission } from '@/types'

// 用户相关接口
export const userAPI = {
  // 分页查询用户
  getUserPage: (params: any) => {
    return http.get('/system/user/page', { params })
  },
  
  // 查询用户列表
  getUserList: () => {
    return http.get('/system/user/list')
  },
  
  // 根据ID查询用户
  getUserById: (id: number) => {
    return http.get(`/system/user/${id}`)
  },
  
  // 创建用户
  createUser: (data: any) => {
    return http.post('/system/user', data)
  },
  
  // 更新用户
  updateUser: (id: number, data: any) => {
    return http.put(`/system/user/${id}`, data)
  },
  
  // 删除用户
  deleteUser: (id: number) => {
    return http.delete(`/system/user/${id}`)
  },
  
  // 更新用户状态
  updateUserStatus: (id: number, status: number) => {
    return http.put(`/system/user/${id}/status?status=${status}`)
  },
  
  // 重置用户密码
  resetUserPassword: (id: number) => {
    return http.post(`/system/user/${id}/reset-password`)
  },
  
  // 解锁用户
  unlockUser: (id: number) => {
    return http.post(`/system/user/${id}/unlock`)
  }
}

// 角色相关接口
export const roleAPI = {
  // 分页查询角色
  getRolePage: (params: any) => {
    return http.get('/system/role/page', { params })
  },
  
  // 查询角色列表
  getRoleList: () => {
    return http.get('/system/role/list')
  },
  
  // 根据ID查询角色
  getRoleById: (id: number) => {
    return http.get(`/system/role/${id}`)
  },
  
  // 查询角色权限
  getRolePermissions: (id: number) => {
    return http.get(`/system/role/${id}/permissions`)
  },
  
  // 查询用户角色
  getUserRoles: (userId: number) => {
    return http.get(`/system/role/user/${userId}`)
  },
  
  // 创建角色
  createRole: (data: any) => {
    return http.post('/system/role', data)
  },
  
  // 更新角色
  updateRole: (id: number, data: any) => {
    return http.put(`/system/role/${id}`, data)
  },
  
  // 删除角色
  deleteRole: (id: number) => {
    return http.delete(`/system/role/${id}`)
  },
  
  // 更新角色状态
  updateRoleStatus: (id: number, status: number) => {
    return http.put(`/system/role/${id}/status?status=${status}`)
  },
  
  // 分配权限
  assignPermissions: (id: number, data: { permissionIds: number[] }) => {
    return http.post(`/system/role/${id}/permissions`, data)
  },
  
  // 分配角色
  assignRoles: (userId: number, data: { roleIds: number[] }) => {
    return http.post(`/system/role/user/${userId}/roles`, data)
  }
}

// 权限相关接口
export const permissionAPI = {
  // 获取权限树
  getPermissionTree: () => {
    return http.get('/system/permission/tree')
  },
  
  // 获取子权限树
  getPermissionTreeByParent: (parentId: number) => {
    return http.get(`/system/permission/tree/${parentId}`)
  },
  
  // 获取用户权限树
  getUserPermissionTree: (userId: number) => {
    return http.get(`/system/permission/user/${userId}/tree`)
  },
  
  // 分页查询权限
  getPermissionPage: (params: any) => {
    return http.get('/system/permission/page', { params })
  },
  
  // 查询权限列表
  getPermissionList: () => {
    return http.get('/system/permission/list')
  },
  
  // 获取菜单权限
  getMenuPermissions: () => {
    return http.get('/system/permission/menu')
  },
  
  // 获取按钮权限
  getButtonPermissions: (parentId: number) => {
    return http.get(`/system/permission/button/${parentId}`)
  },
  
  // 根据ID查询权限
  getPermissionById: (id: number) => {
    return http.get(`/system/permission/${id}`)
  },
  
  // 创建权限
  createPermission: (data: any) => {
    return http.post('/system/permission', data)
  },
  
  // 更新权限
  updatePermission: (id: number, data: any) => {
    return http.put(`/system/permission/${id}`, data)
  },
  
  // 删除权限
  deletePermission: (id: number) => {
    return http.delete(`/system/permission/${id}`)
  },
  
  // 更新权限状态
  updatePermissionStatus: (id: number, status: number) => {
    return http.put(`/system/permission/${id}/status?status=${status}`)
  }
}

// 保持向后兼容的别名
export const userApi = userAPI
export const roleApi = roleAPI

export const deptApi = {
  // 获取部门树
  getDeptTree: (): Promise<any[]> => {
    return http.get('/system/depts/tree')
  },
  
  // 获取部门列表
  getDepts: (params?: {
    deptName?: string
    status?: number
  }): Promise<any[]> => {
    return http.get('/system/depts', { params })
  },
  
  // 获取部门详情
  getDept: (id: number): Promise<any> => {
    return http.get(`/system/depts/${id}`)
  },
  
  // 创建部门
  createDept: (data: any): Promise<any> => {
    return http.post('/system/depts', data)
  },
  
  // 更新部门
  updateDept: (id: number, data: any): Promise<any> => {
    return http.put(`/system/depts/${id}`, data)
  },
  
  // 删除部门
  deleteDept: (id: number): Promise<void> => {
    return http.delete(`/system/depts/${id}`)
  },
}