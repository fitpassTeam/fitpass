package org.example.fitpass.domain.notify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.notify.service.NotifyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "NOTIFY API", description = "SSE를 통해 실시간 알림을 구독하는 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notify")
public class NotifyController {

    private final NotifyService notifyService;

    @Operation(
        summary = "알림 구독 (SSE)",
        description = """
                클라이언트가 서버로부터 실시간 알림을 받기 위해 구독합니다.  
                SSE 방식으로 작동하며, 끊긴 이후 재연결 시 `Last-Event-ID` 헤더를 통해 마지막 수신 이벤트 ID를 보내야 누락 없이 알림을 받을 수 있습니다.
                """,
        parameters = {
            @Parameter(
                name = "Last-Event-ID",
                description = "클라이언트가 마지막으로 받은 이벤트 ID (재연결 시 사용)",
                required = false,
                example = "1706420834577"
            )
        }
    )
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(
        @Parameter(hidden = true)
        @AuthenticationPrincipal CustomUserDetails user,
        @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "")
        String lastEventId
    ) {
        return notifyService.subscribe(user.getId(), lastEventId);
    }


}
