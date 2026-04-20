package com.smartwaste.repository;

import com.smartwaste.model.CitizenReport;
import com.smartwaste.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitizenReportRepository extends JpaRepository<CitizenReport, Long> {

    List<CitizenReport> findByStatusOrderByTimestampDesc(ReportStatus status);

    List<CitizenReport> findAllByOrderByTimestampDesc();

    long countByStatus(ReportStatus status);
}
