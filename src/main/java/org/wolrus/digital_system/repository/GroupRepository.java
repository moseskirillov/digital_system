package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wolrus.digital_system.entity.GroupEntity;


public interface GroupRepository extends JpaRepository<GroupEntity, Integer> {
    Integer countAllByIsOpenIsTrueAndAge(String type);
}
