package org.wolrus.digital_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "groups", schema = "homegroups_bot")
public class GroupEntity {

    @Id
    private Integer id;
    private LocalTime time;
    private String age;
    private String type;
    private String description;

    @Column(name = "is_open")
    private Boolean isOpen = false;

    @Column(name = "is_overflow")
    private Boolean isOverflow = false;

    @Column(name = "is_multi_day")
    private Boolean isMultiDay = false;

    @JoinColumn(name = "leader_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupLeaderEntity leader;

    @JoinColumn(name = "district_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private DistrictEntity district;

    @OneToMany(mappedBy = "group")
    private Set<GroupDayEntity> groupsDays = new LinkedHashSet<>();

    @OneToMany(mappedBy = "group")
    private Set<GroupStationEntity> groupsStations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "group")
    private Set<ReportEntity> reports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "group")
    private Set<RequestEntity> requests = new LinkedHashSet<>();

}