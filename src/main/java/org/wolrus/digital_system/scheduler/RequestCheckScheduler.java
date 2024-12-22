package org.wolrus.digital_system.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wolrus.digital_system.feign.TelegramReportClient;
import org.wolrus.digital_system.model.TelegramRequest;
import org.wolrus.digital_system.repository.RequestRepository;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class RequestCheckScheduler {

    private static final String MESSAGE_TEXT = "Добрый день, вчера вы оставляли заявку на присоединение к домашней группе. " +
            "Связались ли с вами?";

    private final RequestRepository requestRepository;
    private final TelegramReportClient telegramReportClient;

    @Transactional
    @Scheduled(cron = "0 0 20 * * ?", zone = "Europe/Moscow")
    public void requestCheck() {
        var now = ZonedDateTime.now().minusDays(1L);
        var startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        var endOfDay = startOfDay.plusDays(1).minusNanos(1);
        var startOfDayInstant = startOfDay.toLocalDateTime().atZone(ZoneOffset.UTC).toInstant();
        var endOfDayInstant = endOfDay.toLocalDateTime().atZone(ZoneOffset.UTC).toInstant();
        var tomorrowRequests = requestRepository.findAllByFeedbackRequestedFalseAndDateBetween(startOfDayInstant, endOfDayInstant);
        for (var request : tomorrowRequests) {
            var telegramId = request.getUser().getTelegramId();
            var feedbackRequest = TelegramRequest.feedbackKeyboard(request.getId(), telegramId, MESSAGE_TEXT);
            telegramReportClient.sendMessage(feedbackRequest);
            request.setFeedbackRequested(true);
        }
        requestRepository.saveAll(tomorrowRequests);
    }

}
