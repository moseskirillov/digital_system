package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wolrus.digital_system.entity.UnfilledReportEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UnfilledReportRepository extends JpaRepository<UnfilledReportEntity, Long> {
    Optional<UnfilledReportEntity> findByLeaderNameAndReportDateAndGroup_Id(String leaderName, LocalDate reportDate, Integer groupId);

    List<UnfilledReportEntity> findAllByReportDateLessThanEqual(LocalDate date);
}
