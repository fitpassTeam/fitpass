package org.example.fitpass.domain.notify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.fitpass.domain.notify.NotificationType;

import java.time.LocalDateTime;

@Schema(description = "실시간 알림 이벤트 DTO")
public record NotificationEvent(

    @Schema(description = "알림 수신자의 ID", example = "42")
    Long receiverId,

    @Schema(description = "알림 수신자 유형 (USER 또는 TRAINER)", example = "USER")
    String receiverType,

    @Schema(description = "알림 유형 (ex: NEW_REVIEW, NEW_LIKE)", implementation = NotificationType.class)
    NotificationType notificationType,

    @Schema(description = "알림에 표시될 내용", example = "새로운 후기가 등록되었습니다.")
    String content,

    @Schema(description = "클릭 시 이동할 URL", example = "/gyms/15")
    String url,

    @Schema(description = "알림 생성 시각", example = "2025-07-02T20:15:30")
    LocalDateTime createdAt
) {}