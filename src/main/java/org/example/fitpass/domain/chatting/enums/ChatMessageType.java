package org.example.fitpass.domain.chatting.enums;

import java.util.Arrays;

public enum ChatMessageType {
    ENTER, TALK;

    public static ChatMessageType of(String type){
        return Arrays.stream(ChatMessageType.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new RuntimeException());
    }
}
