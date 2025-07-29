package com.hospital.report.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报表状态枚举
 */
@Getter
@AllArgsConstructor
public enum ReportStatus {
    
    DRAFT(1, "草稿"),
    PENDING_AUDIT(2, "待审核"),
    APPROVED(3, "审核通过"),
    REJECTED(4, "审核拒绝");

    private final int code;
    private final String description;

    /**
     * 根据状态码获取枚举
     * @param code 状态码
     * @return 枚举值
     */
    public static ReportStatus getByCode(int code) {
        for (ReportStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的报表状态码: " + code);
    }

    /**
     * 检查状态是否可以审核
     * @param status 状态
     * @return 是否可以审核
     */
    public static boolean canAudit(int status) {
        return status == PENDING_AUDIT.getCode();
    }

    /**
     * 检查状态是否可以发布
     * @param status 状态
     * @return 是否可以发布
     */
    public static boolean canPublish(int status) {
        return status == APPROVED.getCode();
    }

    /**
     * 检查状态是否可以重新提交
     * @param status 状态
     * @return 是否可以重新提交
     */
    public static boolean canResubmit(int status) {
        return status == REJECTED.getCode();
    }

    /**
     * 获取状态描述
     * @param code 状态码
     * @return 状态描述
     */
    public static String getDescription(int code) {
        try {
            return getByCode(code).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知状态";
        }
    }
}
