package org.wolrus.digital_system.model;

import lombok.Builder;
import org.wolrus.digital_system.entity.UserEntity;

@Builder
public record LeaderInfo(String name, String telegramId) {

    public static LeaderInfo of(UserEntity leader) {
        return LeaderInfo.builder()
                .name(leader.getLastName() + " " + leader.getFirstName())
                .telegramId(String.valueOf(leader.getTelegramId()))
                .build();
    }

    public static LeaderInfo empty(String adminId) {
        return LeaderInfo.builder()
                .name("Не определен")
                .telegramId(adminId)
                .build();
    }
}
