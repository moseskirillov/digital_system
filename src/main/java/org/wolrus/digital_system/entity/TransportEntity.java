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
@Table(name = "transports", schema = "homegroups_bot")
public class TransportEntity {

    @Id
    private Integer id;
    private String title;

    @Column(name = "callback_data", nullable = false)
    private String callbackData;

    @OneToMany(mappedBy = "transport")
    private Set<StationEntity> stations = new LinkedHashSet<>();

}