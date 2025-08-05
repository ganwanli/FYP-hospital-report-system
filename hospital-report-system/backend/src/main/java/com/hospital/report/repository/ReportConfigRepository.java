package com.hospital.report.repository;

import com.hospital.report.entity.ReportConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  ReportConfigRepository extends CrudRepository<ReportConfig,Long > {

    /**
     * 检查报表代码是否已存在
     * @param reportCode 报表代码
     * @return 是否存在
     */
    boolean existsByReportCode(String reportCode);

}
