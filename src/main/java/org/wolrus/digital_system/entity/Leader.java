package org.wolrus.digital_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "group_leaders", schema = "homegroup_bot")
public class Leader {
    @Id
    private Long id;
    private String name;

    @Column(name  = "telegram_id")
    private Long telegramId;

    @Column(name  = "telegram_login")
    private String telegramLogin;

    @OneToMany
    @JoinColumn(name = "leader_id")
    private List<Group> groups;
}
