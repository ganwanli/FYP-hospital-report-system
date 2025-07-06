import React from 'react'
import { Button, ButtonProps } from 'antd'
import PermissionWrapper from './index'

interface PermissionButtonProps extends ButtonProps {
  permission?: string | string[]
  role?: string | string[]
  requireAll?: boolean
}

const PermissionButton: React.FC<PermissionButtonProps> = ({
  permission,
  role,
  requireAll = false,
  children,
  ...buttonProps
}) => {
  return (
    <PermissionWrapper
      permission={permission}
      role={role}
      requireAll={requireAll}
    >
      <Button {...buttonProps}>
        {children}
      </Button>
    </PermissionWrapper>
  )
}

export default PermissionButton