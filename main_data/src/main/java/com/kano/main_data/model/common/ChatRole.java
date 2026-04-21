package com.kano.main_data.model.common;

import lombok.Getter;

@Getter
public enum ChatRole {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system"),
    TOOL("tool");

    private final String value;

    ChatRole(String value) {
        this.value = value;
    }

    public static ChatRole fromValue(String role) {
        for (ChatRole chatRole : ChatRole.values()) {
            if (chatRole.value.equals(role)) {
                return chatRole;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + role);
    }
}
