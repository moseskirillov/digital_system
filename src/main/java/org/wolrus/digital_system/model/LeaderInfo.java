package org.wolrus.digital_system.model;

import lombok.Builder;
import org.wolrus.digital_system.entity.Leader;

@Builder
public record LeaderInfo(Integer id, String name, String telegramId) {
    public static LeaderInfo of(Leader leader) {
        return LeaderInfo.builder()
                .id(leader.getId())
                .name(leader.getName())
                .telegramId(String.valueOf(leader.getTelegramId()))
                .build();
    }
}
