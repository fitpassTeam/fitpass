package org.example.fitpass.domain.notify.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.fitpass.domain.notify.entity.Notify;

public class NotifyDto {

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Response {
        String id;
        String name;
        String content;
        String type;
        String createdAt;

        public Response(String id, String name, String content, String type, String createdAt) {
            this.id = id;
            this.name = name;
            this.content = content;
            this.type = type;
            this.createdAt = createdAt;
        }

        public static Response createResponse(Notify notify) {
            return new Response(
                notify.getId().toString(),
                notify.getReceiver().getName(),
                notify.getContent(),
                notify.getNotificationType().name(),
                notify.getCreatedAt().toString()
            );
        }
    }
}
