package org.wolrus.digital_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "requests", schema = "homegroups_bot")
public class RequestEntity {

    @Id
    private Integer id;
    private Instant date;
    private String comment;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @Column(name = "is_processed")
    private Boolean isProcessed = false;

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupEntity group;

    @Column(name = "process_date")
    private Instant processDate;

    @Column(name = "feedback_requested")
    private Boolean feedbackRequested;

}