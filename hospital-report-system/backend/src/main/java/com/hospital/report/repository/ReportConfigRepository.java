package com.hospital.report.repository;

import com.hospital.report.entity.ReportConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  ReportConfigRepository extends CrudRepository<ReportConfig,Long > {


}
