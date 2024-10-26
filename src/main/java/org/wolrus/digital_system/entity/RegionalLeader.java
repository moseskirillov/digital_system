package org.wolrus.digital_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "regional_leaders", schema = "homegroup_bot")
public class RegionalLeader {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "telegram_id")
    private Long telegramId;

    @OneToMany(mappedBy = "regionalLeader", fetch = FetchType.LAZY)
    private Set<Leader> groupLeaders;
}
