package org.wolrus.digital_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "lines", schema = "homegroups_bot")
public class LineEntity {

    @Id
    private Integer id;
    private String title;
    private String color;

    @Column(name = "callback_data")
    private String callbackData;

    @OneToMany(mappedBy = "line")
    private Set<StationEntity> stations = new LinkedHashSet<>();

}