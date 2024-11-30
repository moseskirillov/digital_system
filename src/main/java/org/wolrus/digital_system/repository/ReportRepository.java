package org.wolrus.digital_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.wolrus.digital_system.entity.ReportEntity;
import org.wolrus.digital_system.model.ReportForPastor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    @Query(value = """
            select count(*)
            from homegroups_bot.reports r
            where r.group_is_done = true
            and date >= (current_date - interval '1 month')
            """, nativeQuery = true)
    Integer countAllCompletedGroupsByMonth();

    @Query(value = """
            select count(*)
            from homegroups_bot.reports r
            where r.group_is_done = true
            and date >= (current_date - interval '1 week')
            """, nativeQuery = true)
    Integer countAllCompletedGroupsByWeek();

    @Query(value = """
            select count(*)
            from homegroups_bot.reports r
            where r.group_is_done = false
            and r.date >= (current_date - interval '1 month')
            """, nativeQuery = true)
    Integer countAllNotCompletedGroupsByMonth();

    @Query(value = """
            select count(*)
            from homegroups_bot.reports r
            where r.group_is_done = false
            and r.date >= (current_date - interval '1 week')
            """, nativeQuery = true)
    Integer countAllNotCompletedGroupsByWeek();

    @Query(value = """
            select r.n, r.c
            from (select leader_name n, count(leader_name) as c
            from homegroups_bot.reports
            where group_is_done = false
            and date >= (current_date - interval '1 month')
            group by leader_name) r
            where c > 3""", nativeQuery = true)
    List<Object[]> findThreeTimesNotCompletedGroupsByMonth();

    Optional<ReportEntity> findByGroupIdAndDate(Integer group_id, LocalDate date);

    @Query(value = """
            select CONCAT(SUBSTRING(rl.name FROM 1 FOR POSITION(' ' IN rl.name) - 1), ' ',
                          LEFT(SUBSTRING(rl.name FROM POSITION(' ' IN rl.name) + 1), 1)) as regionalLeaderName,
                   SUBSTRING(TO_CHAR(r.date, 'TMMonth') FROM 1 FOR 3)                    as month,
                   sum(case when group_is_done = true then 1 else 0 end)                 as isDone,
                   sum(case when group_is_done = false then 1 else 0 end)                as isNotDone,
                   sum(r.people_count)                                                   as personCount
            from main_db.homegroup_bot.reports r
                     left join main_db.homegroup_bot.group_leaders gl on gl.name = r.leader_name
                     left join main_db.homegroup_bot.regional_leaders rl on rl.id = gl.region_leader_id
            where date >= (current_date - interval '1 month')
            group by rl.name, month
            order by rl.name, month desc;
            """, nativeQuery = true)
    List<ReportForPastor> reportForPastor();

}
