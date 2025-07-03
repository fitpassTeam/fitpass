package org.example.fitpass.domain.notify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.fitpass.domain.notify.entity.Notify;

public class NotifyDto {

    @NoArgsConstructor
    @Getter
    @Setter
    @Schema(description = "알림 응답 DTO")
    public static class Response {

        @Schema(description = "알림 ID", example = "123")
        private String id;

        @Schema(description = "알림 수신자 이름", example = "홍길동")
        private String name;

        @Schema(description = "알림 내용", example = "새 댓글이 등록되었습니다.")
        private String content;

        @Schema(description = "알림 타입", example = "NEW_COMMENT")
        private String type;

        @Schema(description = "알림 생성 일시", example = "2025-07-02T20:00:00")
        private String createdAt;

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
                notify.getReceiverName(),
                notify.getContent(),
                notify.getNotificationType().name(),
                notify.getCreatedAt().toString()
            );
        }
    }
}