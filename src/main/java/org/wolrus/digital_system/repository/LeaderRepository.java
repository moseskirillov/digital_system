package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.wolrus.digital_system.entity.Leader;

import java.util.List;

public interface LeaderRepository extends JpaRepository<Leader, Long> {
    @Query("""
            select l from Leader l
            join fetch l.groups g
            join fetch l.regionalLeader rl
            where g.isOpen and g.day = :day
            and l.telegramId is not null
            and l.telegramLogin != 'Pelna'
            and l.telegramLogin != 'mariKirillova3'
            and g.age != 'Молодежные (до 25)'
            and g.age != 'Молодежные (после 25)'
            and g.age != 'Подростки'
            and l.regionalLeader.id is not null
            and l.regionalLeader.id != :regionLeaderId
            """)
    List<Leader> findGroupLeadersByDay(String day, Integer regionLeaderId);
}
