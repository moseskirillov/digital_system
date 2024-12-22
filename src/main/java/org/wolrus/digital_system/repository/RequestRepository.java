package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.wolrus.digital_system.entity.RequestEntity;

import java.time.Instant;
import java.util.List;

public interface RequestRepository extends JpaRepository<RequestEntity, Integer> {
    @EntityGraph(attributePaths = {"user", "group", "group.leader", "group.leader.user"})
    List<RequestEntity> findAllByFeedbackRequestedFalseAndDateBetween(Instant startDay, Instant endDay);
}
