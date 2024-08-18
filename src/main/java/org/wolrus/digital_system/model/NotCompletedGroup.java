package org.wolrus.digital_system.model;

public record NotCompletedGroup(
        String name,
        Integer count
) {
    public static NotCompletedGroup of(Object[] name) {
        return new NotCompletedGroup(
                name != null && name[0] != null ? name[0].toString() : "Имя не определено",
                name != null && name[1] != null ? Integer.parseInt(name[1].toString()) : 0
        );
    }
}
