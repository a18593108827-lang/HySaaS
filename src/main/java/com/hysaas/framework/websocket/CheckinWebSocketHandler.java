package com.hysaas.framework.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CheckinWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long eventId = parseEventId(session);
        if (eventId == null) {
            close(session);
            return;
        }
        sessions.computeIfAbsent(eventId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long eventId = parseEventId(session);
        if (eventId == null) {
            return;
        }
        Set<WebSocketSession> set = sessions.get(eventId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                sessions.remove(eventId);
            }
        }
    }

    public void broadcast(Long eventId, int count, int total) {
        Set<WebSocketSession> set = sessions.get(eventId);
        if (set == null || set.isEmpty()) {
            return;
        }
        String payload = "{\"count\":" + count + ",\"total\":" + total + "}";
        TextMessage message = new TextMessage(payload);
        for (WebSocketSession session : set) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    log.warn("ws send failed: eventId={}", eventId, e);
                }
            }
        }
    }

    private Long parseEventId(WebSocketSession session) {
        String path = session.getUri() == null ? "" : session.getUri().getPath();
        int idx = path.lastIndexOf('/');
        if (idx < 0 || idx == path.length() - 1) {
            return null;
        }
        try {
            return Long.parseLong(path.substring(idx + 1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void close(WebSocketSession session) {
        try {
            session.close(CloseStatus.BAD_DATA);
        } catch (IOException ignored) {
        }
    }
}
