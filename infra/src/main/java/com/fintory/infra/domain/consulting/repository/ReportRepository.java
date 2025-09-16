package com.fintory.infra.domain.consulting.repository;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.consulting.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report,Long> {

    Optional<Report> findByReportMonthAndChild(String date, Child child);
}