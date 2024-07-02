package org.wolrus.digital_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wolrus.digital_system.model.ReportRequest;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reports", schema = "homegroup_bot")
public class Report {

    @Transient
    private static final String YES = "Да";

    @Id
    @SequenceGenerator(name = "reports_seq", allocationSize = 1, schema = "homegroup_bot")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reports_seq")
    private Long id;

    @Column(name = "leader_name")
    private String leaderName;

    @Column(name = "group_is_done")
    private Boolean groupIsDone;

    @Column(name = "people_count")
    private Integer peopleCount;

    private LocalDate date;
    private String evidence;

    @Column(name = "meet_with_senior")
    private Boolean meetWithSenior;

    public static Report of(ReportRequest report) {
        return Report.builder()
                .date(LocalDate.now())
                .leaderName(report.name())
                .groupIsDone(YES.equals(report.groupIsDone()))
                .peopleCount(report.peopleCount())
                .evidence(report.evidence())
                .meetWithSenior(YES.equals(report.meetWithSenior()))
                .build();
    }

}
