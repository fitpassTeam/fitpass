package org.example.fitpass.domain.notify.controller;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.notify.service.NotifyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notify")
public class NotifyController {

    private final NotifyService notifyService;

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails user,
        @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "")String lastEventId){
        return notifyService.subscribe(user.getId(), lastEventId);
    }


}
