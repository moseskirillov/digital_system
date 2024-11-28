package org.wolrus.digital_system.repository.v2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wolrus.digital_system.entity.v2.RegionalLeaderEntity;

import java.util.List;

public interface RegionalLeaderRepository extends JpaRepository<RegionalLeaderEntity, Integer> {
    List<RegionalLeaderEntity> findAllByUser_TelegramIdNotNull();
}
