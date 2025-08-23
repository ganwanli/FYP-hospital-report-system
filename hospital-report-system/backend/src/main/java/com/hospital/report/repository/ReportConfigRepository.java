package com.hospital.report.repository;

import com.hospital.report.entity.ReportConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface  ReportConfigRepository extends CrudRepository<ReportConfig,Long > {

    /**
     * 检查报表代码是否已存在
     * @param reportCode 报表代码
     * @return 是否存在
     */
    boolean existsByReportCode(String reportCode);

    /**
     * 根据前缀查找报表代码
     * @param prefix 前缀
     * @return 匹配的报表代码列表
     */
    @Query("SELECT r.reportCode FROM ReportConfig r WHERE r.reportCode LIKE :prefix%")
    List<String> findReportCodesByPrefix(@Param("prefix") String prefix);

    /**
     * 根据关联的子报表ID查找父报表
     * @param linkedReportId 关联的子报表ID
     * @return 父报表列表
     */
    List<ReportConfig> findByLinkedReportId(Long linkedReportId);

}
