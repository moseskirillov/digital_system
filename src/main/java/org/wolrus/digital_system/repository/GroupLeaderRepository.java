package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.wolrus.digital_system.entity.GroupLeaderEntity;

import java.util.List;

public interface GroupLeaderRepository extends JpaRepository<GroupLeaderEntity, Integer> {
    @EntityGraph(attributePaths = {"user", "groups"})
    List<GroupLeaderEntity> findAllByGroups_GroupsDays_Day_TitleAndRegionalLeader_IdNotAndGroups_Age(String title, Integer regionLeaderId, String age);

    @EntityGraph(attributePaths = {"user", "groups"})
    List<GroupLeaderEntity> findAllByGroups_IsOpenAndGroups_Age(Boolean isOpen, String age);
}
