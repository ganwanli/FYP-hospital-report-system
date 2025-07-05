import { http } from '@/utils/request'
import type { User, Role, Dept, PageParams, PageResult } from '@/types'

export const userApi = {
  // 获取用户列表
  getUsers: (params: PageParams & {
    username?: string
    realName?: string
    deptId?: number
    status?: number
  }): Promise<PageResult<User>> => {
    return http.get('/system/users', { params })
  },
  
  // 获取用户详情
  getUser: (id: number): Promise<User> => {
    return http.get(`/system/users/${id}`)
  },
  
  // 创建用户
  createUser: (data: Partial<User>): Promise<User> => {
    return http.post('/system/users', data)
  },
  
  // 更新用户
  updateUser: (id: number, data: Partial<User>): Promise<User> => {
    return http.put(`/system/users/${id}`, data)
  },
  
  // 删除用户
  deleteUser: (id: number): Promise<void> => {
    return http.delete(`/system/users/${id}`)
  },
  
  // 重置密码
  resetPassword: (id: number, password: string): Promise<void> => {
    return http.post(`/system/users/${id}/reset-password`, { password })
  },
}

export const roleApi = {
  // 获取角色列表
  getRoles: (params?: PageParams & {
    roleName?: string
    roleCode?: string
    status?: number
  }): Promise<PageResult<Role>> => {
    return http.get('/system/roles', { params })
  },
  
  // 获取所有角色（不分页）
  getAllRoles: (): Promise<Role[]> => {
    return http.get('/system/roles/all')
  },
  
  // 获取角色详情
  getRole: (id: number): Promise<Role> => {
    return http.get(`/system/roles/${id}`)
  },
  
  // 创建角色
  createRole: (data: Partial<Role>): Promise<Role> => {
    return http.post('/system/roles', data)
  },
  
  // 更新角色
  updateRole: (id: number, data: Partial<Role>): Promise<Role> => {
    return http.put(`/system/roles/${id}`, data)
  },
  
  // 删除角色
  deleteRole: (id: number): Promise<void> => {
    return http.delete(`/system/roles/${id}`)
  },
  
  // 分配权限
  assignPermissions: (roleId: number, permissionIds: number[]): Promise<void> => {
    return http.post(`/system/roles/${roleId}/permissions`, { permissionIds })
  },
}

export const deptApi = {
  // 获取部门树
  getDeptTree: (): Promise<Dept[]> => {
    return http.get('/system/depts/tree')
  },
  
  // 获取部门列表
  getDepts: (params?: {
    deptName?: string
    status?: number
  }): Promise<Dept[]> => {
    return http.get('/system/depts', { params })
  },
  
  // 获取部门详情
  getDept: (id: number): Promise<Dept> => {
    return http.get(`/system/depts/${id}`)
  },
  
  // 创建部门
  createDept: (data: Partial<Dept>): Promise<Dept> => {
    return http.post('/system/depts', data)
  },
  
  // 更新部门
  updateDept: (id: number, data: Partial<Dept>): Promise<Dept> => {
    return http.put(`/system/depts/${id}`, data)
  },
  
  // 删除部门
  deleteDept: (id: number): Promise<void> => {
    return http.delete(`/system/depts/${id}`)
  },
}