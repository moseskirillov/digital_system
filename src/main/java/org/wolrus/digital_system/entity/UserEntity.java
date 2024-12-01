package org.wolrus.digital_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "homegroups_bot")
public class UserEntity {

    @Id
    private Integer id;
    private String phone;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "telegram_login")
    private String telegramLogin;

    @Column(name = "telegram_id")
    private String telegramId;

    @Column(name = "is_admin")
    private Boolean isAdmin = false;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "is_youth_admin")
    private Boolean isYouthAdmin = false;

    public String getFullName() {
        return this.getLastName() + " " + this.getFirstName();
    }

}