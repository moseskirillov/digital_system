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
import org.wolrus.digital_system.model.NotCompletedGroup;
import org.wolrus.digital_system.repository.GroupRepository;
import org.wolrus.digital_system.repository.LeaderRepository;
import org.wolrus.digital_system.repository.ReportRepository;
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

    @Value("${yandex.form.url}")
    private String YANDEX_FORM_URL;

    @Value("${data.region-leader.id}")
    private Integer REGION_LEADER_ID;

    @Value("${telegram.pastor-id}")
    private String PASTOR_TELEGRAM_ID;

    @Value("${telegram.groups-admin-id}")
    private String GROUPS_ADMIN_TELEGRAM_ID;

    private static final String HOME_GROUP_TYPE = "Взрослые";
    private static final Locale LOCALE = Locale.of("ru", "RU");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String REPORT_MESSAGE = "Добрый вечер, сегодня у вас проходит домашняя группа. " +
            "Пожалуйста, не забудьте заполнить отчет по ссылке: %s";
    private static final String REPEATED_REPORT_MESSAGE = "Добрый вечер, у вас есть незаполненный отчет. " +
            "Пожалуйста, заполните его по ссылке: %s";

    private final GroupRepository groupRepository;
    private final UrlShorterClient urlShorterClient;
    private final LeaderRepository leaderRepository;
    private final ReportRepository reportRepository;
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
        var unfilledReports = unfilledReportRepository.findAllByDate(now);
        if (unfilledReports.isEmpty()) {
            log.info("Нет незаполненных отчетов");
            return;
        }
        var leadersMapInfo = createLeadersMapInfo();
        for (var unfilledReport : unfilledReports) {
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
        }
        var message = createReportText(unfilledReports.size(), leadersMapInfo);
        notificationService.sendNotification(GROUPS_ADMIN_TELEGRAM_ID, message);
        notificationService.sendNotification(PASTOR_TELEGRAM_ID, message);
        log.info("Сообщения о незаполненных отчетах отправлены");
    }

    @Scheduled(cron = "0 0 19 1 * *", zone = "Europe/Moscow")
    public void notifyReportsCount() {
        log.info("Запуск рассылки месячного отчета");
        var openedGroups = groupRepository.countAllByIsOpenIsTrueAndAge(HOME_GROUP_TYPE);
        log.info("Общее количество открытых групп: {}", openedGroups);
        var completedGroupsByMonth = reportRepository.countAllCompletedGroupsByMonth();
        log.info("Общее количество прошедших групп: {}", completedGroupsByMonth);
        var notCompleted = reportRepository.countAllNotCompletedGroupsByMonth();
        log.info("Общее количество не прошедших групп: {}", notCompleted);
        var notCompletedGroups = reportRepository.findThreeTimesNotCompletedGroupsByMonth()
                .stream()
                .map(NotCompletedGroup::of)
                .toList();
        log.info("Общее количество групп, не прошедших три раза: {}", notCompletedGroups.size());
        var text = groupsReportText(openedGroups, completedGroupsByMonth, notCompleted, notCompletedGroups);
        notificationService.sendNotification(GROUPS_ADMIN_TELEGRAM_ID, text);
        notificationService.sendNotification(PASTOR_TELEGRAM_ID, text);
        log.info("Рассылка месячного отчета завершена");
    }

    private void notifyReportsLeaders() {
        log.info("Запуск рассылки напоминаний о заполнении отчетов");
        var currentWeekDay = new GregorianCalendar(LOCALE).getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, LOCALE);
        var formattedDay = currentWeekDay.substring(0, 1).toUpperCase() + currentWeekDay.substring(1);
        log.info("Определен день недели: {}", formattedDay);
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
            log.info("Отправка напоминания лидеру: {}", leaderName);
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
        log.info("Общее количество незаполненных отчетов: {}", reports.size());
        for (var report : reports) {
            var leaderName = report.getLeaderName();
            var leaderTelegramId = report.getLeaderTelegramId();
            var reportDate = report.getReportDate();
            var formattedDate = DATE_FORMATTER.format(reportDate);
            log.info("Отправка повторного напоминания лидеру: {} за дату: {}", leaderName, reportDate);
            notificationService.sendNotification(leaderTelegramId, String.format(REPEATED_REPORT_MESSAGE, getShortUrl(leaderName, formattedDate)));
        }
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

    private String groupsReportText(Integer openedGroups,
                                    Integer completedGroupsByMonth,
                                    Integer notCompleted,
                                    List<NotCompletedGroup> notCompletedGroups) {
        var title = String.format("Отчет по домашним группам за %s\n\n", getMonthName(LocalDate.now().getMonthValue() - 1));
        return title +
               String.format("Открыто групп: %s\n", openedGroups) +
               String.format("Прошло групп: %s\n", completedGroupsByMonth) +
               String.format("Не прошло групп: %s\n\n", notCompleted) +
               notCompletedGroupsReportText(notCompletedGroups);
    }

    private StringBuilder notCompletedGroupsReportText(List<NotCompletedGroup> notCompletedGroups) {
        var sb = new StringBuilder();
        for (var notCompletedGroup : notCompletedGroups) {
            sb.append(String.format("%s, не прошла: %s раз(а)\n", notCompletedGroup.name(), notCompletedGroup.count()));
        }
        return sb;
    }

    private String getMonthName(int number) {
        return switch (number) {
            case 1 -> "Январь";
            case 2 -> "Февраль";
            case 3 -> "Март";
            case 4 -> "Апрель";
            case 5 -> "Май";
            case 6 -> "Июнь";
            case 7 -> "Июль";
            case 8 -> "Август";
            case 9 -> "Сентябрь";
            case 10 -> "Октябрь";
            case 11 -> "Ноябрь";
            case 12 -> "Декабрь";
            default -> throw new IllegalStateException("Ошибка опредения меясца: " + number);
        };
    }
}
