package org.wolrus.digital_system.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wolrus.digital_system.entity.Leader;
import org.wolrus.digital_system.entity.RegionalLeader;
import org.wolrus.digital_system.entity.UnfilledReport;
import org.wolrus.digital_system.feign.UrlShorterClient;
import org.wolrus.digital_system.model.LeaderInfo;
import org.wolrus.digital_system.repository.LeaderRepository;
import org.wolrus.digital_system.repository.UnfilledReportRepository;
import org.wolrus.digital_system.service.NotificationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledComponent {

    @Value("${telegram.groups-admin-id}")
    private String GROUPS_ADMIN_TELEGRAM_ID;

    @Value("${yandex.form.url}")
    private String YANDEX_FORM_URL;

    @Value("${data.region-leader.id}")
    private Integer REGION_LEADER_ID;

    private static final Locale LOCALE = Locale.of("ru", "RU");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String REPORT_MESSAGE = "Добрый вечер, сегодня у вас проходит домашняя группа. " +
            "Пожалуйста, не забудьте заполнить отчет по ссылке: %s";
    private static final String REPEATED_REPORT_MESSAGE = "Добрый вечер, у вас есть незаполненный отчет. " +
            "Пожалуйста, заполните его по ссылке: %s";

    private final UrlShorterClient urlShorterClient;
    private final LeaderRepository leaderRepository;
    private final NotificationService notificationService;
    private final UnfilledReportRepository unfilledReportRepository;

    @Scheduled(cron = "0 0 21 * * *", zone = "Europe/Moscow")
    public void notifyLeadersScheduler() {
        log.info("Запущена ежедневная рассылка напоминаний о заполнении отчетов");
        notifyUnfilledReportsLeaders();
        notifyReportsLeaders();
        log.info("Ежедневная рассылка напоминаний о заполнении отчетов завершена");
    }

    @Scheduled(cron = "0 0 19 * * MON", zone = "Europe/Moscow")
    public void notifyAdmin() {
        log.info("Запуск рассылки информации о незаполненных отчетах");
        var now = LocalDate.now().minusDays(2);
        var unfilledReports = unfilledReportRepository.findALLByDate(now);
        if (unfilledReports.isEmpty()) {
            log.info("Нет незаполненных отчетов");
            return;
        }
        var leadersMapInfo = createLeadersMapInfo();
        unfilledReports.forEach(unfilledReport -> {
            var leaderId = unfilledReport.getLeaderId();
            var leader = leaderRepository.findById(Long.valueOf(leaderId));
            var leaderName = unfilledReport.getLeaderName();
            var date = unfilledReport.getReportDate();
            var regionalLeaderName = leader.map(Leader::getRegionalLeader)
                    .map(RegionalLeader::getName)
                    .orElse("Не определен");
            if (!leadersMapInfo.containsKey(regionalLeaderName)) {
                leadersMapInfo.put(regionalLeaderName, new ArrayList<>());
            }
            leadersMapInfo.get(regionalLeaderName).add(String.format("%s за %s", leaderName, DATE_FORMATTER.format(date)));
        });
        var message = createReportText(unfilledReports.size(), leadersMapInfo);
        notificationService.sendNotification(GROUPS_ADMIN_TELEGRAM_ID, message);
        log.info("Сообщение о незаполненных отчетах отправлено администратору");
    }

    private void notifyReportsLeaders() {
        log.info("Запуск рассылки напоминаний о заполнении отчетов");
        var currentWeekDay = new GregorianCalendar(LOCALE).getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, LOCALE);
        var formattedDay = currentWeekDay.substring(0, 1).toUpperCase() + currentWeekDay.substring(1);
        log.info(String.format("Определен день недели: %s", formattedDay));
        var leaders = leaderRepository.findGroupLeadersByDay(formattedDay, REGION_LEADER_ID)
                .stream()
                .map(LeaderInfo::of)
                .toList();
        for (var leaderInfo : leaders) {
            var now = LocalDate.now();
            var nowAsString = DATE_FORMATTER.format(now);
            var leaderName = leaderInfo.name();
            var telegramId = leaderInfo.telegramId();
            var shortUrl = getShortUrl(leaderName, nowAsString);
            var message = String.format(REPORT_MESSAGE, shortUrl);
            log.info(String.format("Отправка напоминания лидеру: %s", leaderName));
            notificationService.sendNotification(telegramId, message);
            var unfilledReport = UnfilledReport.builder()
                    .reportDate(now)
                    .leaderId(leaderInfo.id())
                    .leaderName(leaderName)
                    .leaderTelegramId(telegramId)
                    .build();
            unfilledReportRepository.save(unfilledReport);
        }
        log.info("Запуск рассылки напоминаний о заполнении отчетов завершен");
    }

    private void notifyUnfilledReportsLeaders() {
        log.info("Запуск рассылки повторных напоминаний о заполнении отчетов");
        var reports = unfilledReportRepository.findAll();
        log.info(String.format("Общее количество незаполненных отчетов: <b>%s</b>", reports.size()));
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

    private Map<String, List<String>> createLeadersMapInfo() {
        return new HashMap<>() {
            @Override
            public String toString() {
                var sb = new StringBuilder();
                for (var entry : this.entrySet()) {
                    var value = String.join("\n", entry.getValue());
                    sb.append("Лидер: ")
                            .append("<b>")
                            .append(entry.getKey())
                            .append("</b>")
                            .append(": " + "\n")
                            .append(value)
                            .append("\n")
                            .append("\n");
                }
                return sb.toString();
            }
        };
    }

    private String createReportText(int size, Map<String, List<String>> leadersMapInfo) {
        return String.format("Общее количество незаполненных отчетов: %s \n", size) + "\n" + leadersMapInfo;
    }
}
