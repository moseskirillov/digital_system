package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wolrus.digital_system.entity.RegionalLeaderEntity;

import java.util.List;

public interface RegionalLeaderRepository extends JpaRepository<RegionalLeaderEntity, Integer> {
    List<RegionalLeaderEntity> findAllByUser_TelegramIdNotNull();
}
