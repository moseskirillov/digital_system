package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wolrus.digital_system.entity.UnfilledReport;

import java.time.LocalDate;
import java.util.Optional;

public interface UnfilledReportRepository extends JpaRepository<UnfilledReport, Long> {
    Optional<UnfilledReport> findByLeaderNameAndReportDate(String leaderName, LocalDate reportDate);
}
