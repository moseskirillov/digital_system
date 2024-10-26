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
@Table(name = "groups", schema = "homegroup_bot")
public class Group {
    @Id
    private Integer id;
    private String metro;
    private String day;
    private String age;
    private String type;

    @Column(name = "is_open")
    private Boolean isOpen;
}
