package org.wolrus.digital_system.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wolrus.digital_system.entity.UnfilledReport;
import org.wolrus.digital_system.feign.UrlShorterClient;
import org.wolrus.digital_system.model.LeaderInfo;
import org.wolrus.digital_system.repository.LeaderRepository;
import org.wolrus.digital_system.repository.UnfilledReportRepository;
import org.wolrus.digital_system.service.NotificationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledComponent {

    @Value("${yandex.form.url}")
    private String YANDEX_FORM_URL;

    private static final Locale LOCALE = Locale.of("ru", "RU");
    private static final Calendar CALENDAR = new GregorianCalendar(LOCALE);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String REPORT_MESSAGE = "Добрый вечер, сегодня у вас проходит домашняя группа. " +
            "Пожалуйста, не забудьте заполнить отчет по ссылке: %s";
    private static final String REPEATED_REPORT_MESSAGE = "Добрый вечер, у вас есть незаполненный отчет. " +
            "Пожалуйста, заполните его по ссылке: %s";

    private final UrlShorterClient urlShorterClient;
    private final LeaderRepository leaderRepository;
    private final NotificationService notificationService;
    private final UnfilledReportRepository unfilledReportRepository;

    @Scheduled(cron = "0 0 21 * * ?")
    public void notifyLeadersScheduler() {
        log.info("Запущена ежедневная рассылка напоминаний о заполнении отчетов");
        notifyUnfilledReportsLeaders();
        notifyReportsLeaders();
        log.info("Ежедневная рассылка напоминаний о заполнении отчетов завершена");
    }

    private void notifyReportsLeaders() {
        log.info("Запуск рассылки напоминаний о заполнении отчетов");
        var currentWeekDay = CALENDAR.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, LOCALE);
        var formattedDay = currentWeekDay.substring(0, 1).toUpperCase() + currentWeekDay.substring(1);
        log.info(String.format("Определен день недели: %s", formattedDay));
        var leaders = leaderRepository.findGroupLeadersByDay(formattedDay)
                .stream()
                .map(e -> new LeaderInfo(e.getName(), String.valueOf(e.getTelegramId())))
                .toList();
        leaders.forEach(leaderInfo -> {
            var leaderName = leaderInfo.name();
            log.info(String.format("Отправка напоминания лидеру: %s", leaderName));
            var telegramId = leaderInfo.telegramId();
            var now = DATE_FORMATTER.format(LocalDate.now());
            var shortUrl = getShortUrl(leaderName, now);
            var message = String.format(REPORT_MESSAGE, shortUrl);
            var unfilledReport = UnfilledReport.builder()
                    .reportDate(LocalDate.now())
                    .leaderName(leaderName)
                    .leaderTelegramId(telegramId)
                    .build();
            unfilledReportRepository.saveAndFlush(unfilledReport);
            notificationService.sendNotification(telegramId, message);
        });
        log.info("Запуск рассылки напоминаний о заполнении отчетов завершен");
    }

    private void notifyUnfilledReportsLeaders() {
        log.info("Запуск рассылки повторных напоминаний о заполнении отчетов");
        var reports = unfilledReportRepository.findAll();
        log.info(String.format("Общее количество незаполненных отчетов: %s", reports.size()));
        reports.forEach(report -> {
            var leaderName = report.getLeaderName();
            var leaderTelegramId = report.getLeaderTelegramId();
            var reportDate = report.getReportDate();
            var formattedDate = DATE_FORMATTER.format(reportDate);
            log.info(String.format("Отправка повторного напоминания лидеру: %s за дату: %s", leaderName, reportDate));
            notificationService.sendNotification(leaderTelegramId, String.format(REPEATED_REPORT_MESSAGE, getShortUrl(leaderName, formattedDate)));
        });
        log.info("Запуск рассылки повторных напоминаний о заполнении отчетов завершен");
    }

    private String getShortUrl(String leaderName, String date) {
        var formUrl = YANDEX_FORM_URL + String.format("?name=%s&date=%s", leaderName.replaceAll(" ", "%20"), date);
        return urlShorterClient.urlShorter(formUrl);
    }

}
