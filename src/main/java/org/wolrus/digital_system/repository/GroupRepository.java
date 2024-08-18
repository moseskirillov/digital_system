package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wolrus.digital_system.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Integer countAllByIsOpenIsTrue();
}
