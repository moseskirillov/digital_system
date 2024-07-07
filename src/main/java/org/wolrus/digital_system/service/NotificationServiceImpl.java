package org.wolrus.digital_system.service;

import feign.FeignException;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.wolrus.digital_system.feign.TelegramReportClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final String TELEGRAM_SEND_MESSAGE_ERROR = "Ошибка отправки сообщения";

    private final TelegramReportClient telegramReportClient;

    @Override
    @RateLimiter(name = "sendNotification")
    public void sendNotification(String chatId, String message) {
        try {
            telegramReportClient.sendMessage(chatId, message, true);
        } catch (FeignException e) {
            log.error(TELEGRAM_SEND_MESSAGE_ERROR, e);
        }
    }
}
