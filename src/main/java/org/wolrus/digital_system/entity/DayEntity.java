package org.wolrus.digital_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "days", schema = "homegroups_bot")
public class DayEntity {

    @Id
    private Integer id;
    private String title;

}