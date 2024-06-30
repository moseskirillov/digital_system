package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wolrus.digital_system.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {}
