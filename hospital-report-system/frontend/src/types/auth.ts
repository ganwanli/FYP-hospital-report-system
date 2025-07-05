export interface User {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  avatar?: string
  status: number
  deptId?: number
  roles: Role[]
  permissions: string[]
  lastLoginTime?: string
  lastLoginIp?: string
}

export interface Role {
  id: number
  roleName: string
  roleCode: string
  description?: string
  status: number
  permissions: Permission[]
}

export interface Permission {
  id: number
  parentId: number
  permissionName: string
  permissionCode: string
  permissionType: number
  menuUrl?: string
  menuIcon?: string
  sortOrder: number
  status: number
}

export interface Dept {
  id: number
  parentId: number
  deptName: string
  deptCode: string
  deptType: string
  leader?: string
  phone?: string
  email?: string
  status: number
  children?: Dept[]
}

export interface LoginRequest {
  username: string
  password: string
  captcha?: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  user: User
}

export interface RefreshTokenResponse {
  token: string
  refreshToken: string
}