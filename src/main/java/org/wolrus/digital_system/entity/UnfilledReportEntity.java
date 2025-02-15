package org.wolrus.digital_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "unfilled_reports", schema = "homegroups_bot")
public class UnfilledReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unfilled_reports_seq")
    @SequenceGenerator(name = "unfilled_reports_seq", allocationSize = 1, schema = "homegroups_bot")
    private Long id;

    @Column(name = "leader_id")
    private Integer leaderId;

    @Column(name = "leader_name")
    private String leaderName;

    @Column(name = "leader_telegram_id")
    private String leaderTelegramId;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupEntity group;

}
