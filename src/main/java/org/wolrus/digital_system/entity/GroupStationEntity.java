package org.wolrus.digital_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "groups_stations", schema = "homegroups_bot")
public class GroupStationEntity {

    @Id
    private Integer id;

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupEntity group;

    @JoinColumn(name = "station_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private StationEntity station;

}