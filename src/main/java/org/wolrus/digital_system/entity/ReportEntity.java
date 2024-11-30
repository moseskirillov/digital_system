package org.wolrus.digital_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;
import org.wolrus.digital_system.model.ReportRequest;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reports", schema = "homegroups_bot")
public class ReportEntity {

    @Transient
    private static final String YES = "Да";

    @Transient
    private static final String EPMTY_STRING = "";

    @Id
    private Long id;
    private LocalDate date;

    @Column(name = "group_is_done")
    private Boolean groupIsDone;

    @Column(name = "leader_name")
    private String leaderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private GroupEntity group;

    @Column(name = "evidence", length = 3000)
    private String evidence;

    @Column(name = "meet_with_senior")
    private Boolean meetWithSenior;

    @Column(name = "people_count")
    private Integer peopleCount;

    public static ReportEntity of(ReportRequest report, LocalDate date, GroupEntity group) {
        return ReportEntity.builder()
                .date(date)
                .leaderName(report.name())
                .groupIsDone(YES.equals(report.groupIsDone()))
                .peopleCount(report.peopleCount() == null ? 0 : report.peopleCount())
                .evidence(StringUtils.hasLength(report.evidence()) ? report.evidence() : EPMTY_STRING)
                .meetWithSenior(YES.equals(report.meetWithSenior()))
                .group(group)
                .build();
    }

}