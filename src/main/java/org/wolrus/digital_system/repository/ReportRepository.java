package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.wolrus.digital_system.entity.ReportEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    @Query(value = """
            select count(*)
            from homegroup_bot.reports r
            where r.group_is_done = true
            and date >= (current_date - interval '1 month')
            """, nativeQuery = true)
    Integer countAllCompletedGroupsByMonth();

    @Query(value = """
            select count(*)
            from homegroup_bot.reports r
            where r.group_is_done = true
            and date >= (current_date - interval '1 week')
            """, nativeQuery = true)
    Integer countAllCompletedGroupsByWeek();

    @Query(value = """
            select count(*)
            from homegroup_bot.reports r
            where r.group_is_done = false
            and r.date >= (current_date - interval '1 month')
            """, nativeQuery = true)
    Integer countAllNotCompletedGroupsByMonth();

    @Query(value = """
            select count(*)
            from homegroup_bot.reports r
            where r.group_is_done = false
            and r.date >= (current_date - interval '1 week')
            """, nativeQuery = true)
    Integer countAllNotCompletedGroupsByWeek();

    @Query(value = """
            select r.n, r.c
            from (select leader_name n, count(leader_name) as c
            from homegroup_bot.reports
            where group_is_done = false
            and date >= (current_date - interval '1 month')
            group by leader_name) r
            where c > 3""", nativeQuery = true)
    List<Object[]> findThreeTimesNotCompletedGroupsByMonth();

    Optional<ReportEntity> findByGroupIdAndDate(Integer group_id, LocalDate date);

}
