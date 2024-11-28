package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wolrus.digital_system.entity.RegionalLeader;

import java.util.List;

public interface RegionalLeaderRepository extends JpaRepository<RegionalLeader, Integer> {
    List<RegionalLeader> findByTelegramIdIsNotNull();
}
