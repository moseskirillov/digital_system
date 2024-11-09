package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.wolrus.digital_system.entity.UnfilledReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UnfilledReportRepository extends JpaRepository<UnfilledReport, Long> {
    Optional<UnfilledReport> findByLeaderNameAndReportDateAndGroup_Id(String leaderName, LocalDate reportDate, Integer groupId);

    @Query("select u from UnfilledReport u where u.reportDate <= :date")
    List<UnfilledReport> findAllByDate(LocalDate date);
}
