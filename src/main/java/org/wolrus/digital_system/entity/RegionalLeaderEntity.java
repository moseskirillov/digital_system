package org.wolrus.digital_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "regional_leaders", schema = "homegroups_bot")
public class RegionalLeaderEntity {

    @Id
    private Integer id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @OneToMany(mappedBy = "regionalLeader")
    private Set<GroupLeaderEntity> groupsLeaders = new LinkedHashSet<>();

}