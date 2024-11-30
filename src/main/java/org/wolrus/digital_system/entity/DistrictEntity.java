package org.wolrus.digital_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "districts", schema = "homegroups_bot")
public class DistrictEntity {

    @Id
    private Integer id;
    private String title;

    @Column(name = "is_mo")
    private Boolean isMo = false;

}