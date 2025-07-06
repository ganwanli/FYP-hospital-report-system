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
  captchaKey?: string
  rememberMe?: boolean
}

export interface LoginResponse {
  token: string
  refreshToken: string
  expiresIn: number
  tokenType: string
  userInfo: UserInfo
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar: string
  gender: number
  department: string
  position: string
  roles: string[]
  permissions: string[]
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface RefreshTokenResponse {
  token: string
  refreshToken: string
  expiresIn: number
}

export interface AuthState {
  isAuthenticated: boolean
  token: string | null
  refreshToken: string | null
  userInfo: UserInfo | null
  permissions: string[]
  roles: string[]
  login: (loginData: LoginResponse) => void
  logout: () => void
  setToken: (token: string) => void
  setUserInfo: (userInfo: UserInfo) => void
  hasPermission: (permission: string | string[]) => boolean
  hasRole: (role: string | string[]) => boolean
  refreshUserToken: () => Promise<void>
}