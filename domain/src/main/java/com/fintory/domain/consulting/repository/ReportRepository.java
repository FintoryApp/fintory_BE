package com.fintory.domain.consulting.repository;

import com.fintory.domain.consulting.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report,Long> {
    Optional<Report> findByReportMonth(String reportMonth);
}
