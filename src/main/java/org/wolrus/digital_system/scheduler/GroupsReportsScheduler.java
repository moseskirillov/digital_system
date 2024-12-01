package org.wolrus.digital_system.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wolrus.digital_system.entity.GroupEntity;
import org.wolrus.digital_system.entity.GroupLeaderEntity;
import org.wolrus.digital_system.entity.RegionalLeaderEntity;
import org.wolrus.digital_system.entity.UnfilledReportEntity;
import org.wolrus.digital_system.entity.UserEntity;
import org.wolrus.digital_system.feign.UrlShorterClient;
import org.wolrus.digital_system.model.LeaderInfo;
import org.wolrus.digital_system.model.NotCompletedGroup;
import org.wolrus.digital_system.repository.GroupLeaderRepository;
import org.wolrus.digital_system.repository.GroupRepository;
import org.wolrus.digital_system.repository.RegionalLeaderRepository;
import org.wolrus.digital_system.repository.ReportRepository;
import org.wolrus.digital_system.repository.UnfilledReportRepository;
import org.wolrus.digital_system.service.NotificationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupsReportsScheduler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String HOME_GROUP_TYPE = "Взрослые";
    private static final Locale LOCALE = Locale.of("ru", "RU");
    private static final String REPEATED_REPORT_MESSAGE = "Добрый вечер, у вас есть незаполненный отчет за %s. Пожалуйста, заполните его по ссылке: %s";
    private static final String REPORT_MESSAGE = "Добрый вечер, сегодня, %s у вас прошла домашняя группа. Пожалуйста, не забудьте заполнить отчет по ссылке: %s. Спасибо за ваше служение, пусть Бог благословит вас";

    @Value("${yandex.form.url}")
    private String YANDEX_FORM_URL;

    @Value("${telegram.pastor-id}")
    private String PASTOR_TELEGRAM_ID;

    @Value("${telegram.groups-admin-id}")
    private String GROUPS_ADMIN_TELEGRAM_ID;

    @Value("${data.region-leader-for-ignore.adult}")
    private Integer REGION_LEADER_ID_FOR_IGNORE_ADULT;

    @Value("${data.region-leader-for-ignore.young}")
    private Integer REGION_LEADER_ID_FOR_IGNORE_YOUNG;

    private final GroupRepository groupRepository;
    private final ReportRepository reportRepository;
    private final UrlShorterClient urlShorterClient;
    private final NotificationService notificationService;
    private final GroupLeaderRepository groupLeaderRepository;
    private final UnfilledReportRepository unfilledReportRepository;
    private final RegionalLeaderRepository regionalLeaderRepository;

    @Scheduled(cron = "0 0 19 * * MON", zone = "Europe/Moscow")
    public void unfilledReportsNotifier() {
        log.info("Запуск рассылки информации о незаполненных отчетах");
        var now = LocalDate.now().minusDays(2);
        var unfilledReports = unfilledReportRepository.findAllByReportDateLessThanEqual(now);
        if (unfilledReports.isEmpty()) {
            log.info("Нет незаполненных отчетов");
            var ignoreIds = List.of(REGION_LEADER_ID_FOR_IGNORE_ADULT, REGION_LEADER_ID_FOR_IGNORE_YOUNG);
            var regionalLeaders = regionalLeaderRepository.findAllByIdNotIn(ignoreIds).stream()
                    .map(RegionalLeaderEntity::getUser)
                    .map(UserEntity::getTelegramId)
                    .map(String::valueOf)
                    .toList();
            sendMessageAboutNotUnfilledReports(regionalLeaders);
            return;
        }
        var leadersInfoMap = createLeadersInfoMap(unfilledReports);
        var message = createReportText(unfilledReports.size(), leadersInfoMap);
        notificationService.sendNotification(GROUPS_ADMIN_TELEGRAM_ID, message);
        notificationService.sendNotification(PASTOR_TELEGRAM_ID, message);
        for (var element : leadersInfoMap.entrySet()) {
            var sb = new StringBuilder();
            sb.append("Здравствуйте 😇\n");
            sb.append("В вашем регионе есть задолженность по отчету прошлой недели\n\n");
            var reportsList = leadersInfoMap.get(element.getKey());
            for (var report : reportsList) {
                sb.append(String.format("%s \n", report));
            }
            var telegramId = element.getKey().telegramId();
            notificationService.sendNotification(telegramId, sb.toString());
        }
        log.info("Сообщения о незаполненных отчетах отправлены");
    }

    @Scheduled(cron = "0 0 18 * * TUE", zone = "Europe/Moscow")
    public void reportsCountForMonthNotifier() {
        log.info("Запуск рассылки месячного отчета");
        var openedGroups = groupRepository.countAllByIsOpenIsTrueAndAge(HOME_GROUP_TYPE);
        log.info("Общее количество открытых групп за месяц: {}", openedGroups);
        var completedGroupsByMonth = reportRepository.countAllCompletedGroupsByMonth();
        log.info("Общее количество прошедших групп за месяц: {}", completedGroupsByMonth);
        var notCompleted = reportRepository.countAllNotCompletedGroupsByMonth();
        log.info("Общее количество не прошедших групп за месяц: {}", notCompleted);
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

    @Scheduled(cron = "0 0 18 * * TUE", zone = "Europe/Moscow")
    public void reportsCountForWeekNotifier() {
        log.info("Запуск рассылки недельного отчета");
        var openedGroups = groupRepository.countAllByIsOpenIsTrueAndAge(HOME_GROUP_TYPE);
        log.info("Общее количество открытых групп за неделю: {}", openedGroups);
        var completedGroupsByMonth = reportRepository.countAllCompletedGroupsByWeek();
        log.info("Общее количество прошедших групп за неделю: {}", completedGroupsByMonth);
        var notCompleted = reportRepository.countAllNotCompletedGroupsByWeek();
        log.info("Общее количество не прошедших групп за неделю: {}", notCompleted);
        var text = groupsReportText(openedGroups, completedGroupsByMonth, notCompleted, Collections.emptyList());
        notificationService.sendNotification(GROUPS_ADMIN_TELEGRAM_ID, text);
        notificationService.sendNotification(PASTOR_TELEGRAM_ID, text);
        log.info("Рассылка недельного отчета завершена");
    }

    @Scheduled(cron = "0 0 22 * * *", zone = "Europe/Moscow")
    public void completingReportsNotifier() {
        log.info("Запуск рассылки напоминаний о заполнении отчетов");
        var currentWeekDay = new GregorianCalendar(LOCALE).getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, LOCALE);
        var formattedDay = currentWeekDay.substring(0, 1).toUpperCase() + currentWeekDay.substring(1);
        log.info("Определен день недели: {}", formattedDay);
        var leaders = groupLeaderRepository.findAllByGroups_GroupsDays_Day_TitleAndRegionalLeader_IdNotAndGroups_Age(
                formattedDay, REGION_LEADER_ID_FOR_IGNORE_ADULT, HOME_GROUP_TYPE
        );
        for (var leaderEntity : leaders) {
            var now = LocalDate.now();
            var nowAsString = DATE_FORMATTER.format(now);
            var leaderName = leaderEntity.getUser().getLastName() + " " + leaderEntity.getUser().getFirstName();
            var telegramId = leaderEntity.getUser().getTelegramId();
            var groups = leaderEntity.getGroups();
            for (var group : groups) {
                var shortUrl = getShortUrl(leaderName, nowAsString, group.getId());
                var message = String.format(REPORT_MESSAGE, nowAsString, shortUrl);
                log.info("Отправка напоминания лидеру: {}", leaderName);
                notificationService.sendNotification(String.valueOf(telegramId), message);
                var unfilledReport = UnfilledReportEntity.builder()
                        .reportDate(now)
                        .leaderId(leaderEntity.getId())
                        .leaderName(leaderName)
                        .leaderTelegramId(String.valueOf(telegramId))
                        .group(group)
                        .build();
                unfilledReportRepository.save(unfilledReport);
            }
        }
        log.info("Запуск рассылки напоминаний о заполнении отчетов завершен");
    }

    @Scheduled(cron = "0 0 21 * * *", zone = "Europe/Moscow")
    public void unfilledReportsReportsNotifier() {
        log.info("Запуск рассылки повторных напоминаний о заполнении отчетов");
        var reports = unfilledReportRepository.findAll();
        log.info("Общее количество незаполненных отчетов: {}", reports.size());
        for (var report : reports) {
            var leaderName = report.getLeaderName();
            var reportDate = report.getReportDate();
            var formattedDate = DATE_FORMATTER.format(reportDate);
            log.info("Отправка повторного напоминания лидеру: {} за дату: {}", leaderName, reportDate);
            var groupId = Optional.ofNullable(report.getGroup())
                    .map(GroupEntity::getId)
                    .orElse(1);
            var message = String.format(REPEATED_REPORT_MESSAGE, formattedDate, getShortUrl(leaderName, formattedDate, groupId));
            notificationService.sendNotification(report.getLeaderTelegramId(), message);
        }
        log.info("Запуск рассылки повторных напоминаний о заполнении отчетов завершен");
    }

    private void sendMessageAboutNotUnfilledReports(List<String> regionalLeaders) {
        for (var rl : regionalLeaders) {
            notificationService.sendNotification(rl,
                    """
                            Здравствуйте
                            В вашем регионе нет задолженностей по отчетам 😇
                            Спасибо большое
                            """);
        }
        notificationService.sendNotification(
                GROUPS_ADMIN_TELEGRAM_ID,
                "За эту неделю нет задолженностей по отчетам о домашних группах"
        );
    }

    private Map<LeaderInfo, List<String>> createLeadersMapInfo() {
        return new HashMap<>() {
            @Override
            public String toString() {
                var sb = new StringBuilder();
                for (var entry : this.entrySet()) {
                    var value = String.join("\n", entry.getValue());
                    sb.append("Лидер: ")
                            .append("<b>")
                            .append(entry.getKey().name())
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

    private Map<LeaderInfo, List<String>> createLeadersInfoMap(List<UnfilledReportEntity> unfilledReportEntities) {
        var leadersMapInfo = createLeadersMapInfo();
        for (var unfilledReport : unfilledReportEntities) {
            var groupLeaderEntity = groupLeaderRepository.findById(unfilledReport.getLeaderId());
            var leaderName = groupLeaderEntity.map(GroupLeaderEntity::getUser)
                    .map(e -> e.getLastName() + " " + e.getFirstName());
            var regionalLeaderModel = groupLeaderEntity.map(GroupLeaderEntity::getRegionalLeader)
                    .map(RegionalLeaderEntity::getUser)
                    .map(LeaderInfo::of)
                    .orElse(LeaderInfo.empty(GROUPS_ADMIN_TELEGRAM_ID));
            leadersMapInfo.computeIfAbsent(regionalLeaderModel, k -> new ArrayList<>())
                    .add(String.format("%s за %s", leaderName, DATE_FORMATTER.format(unfilledReport.getReportDate())));
        }
        return leadersMapInfo;
    }

    private String createReportText(int size, Map<LeaderInfo, List<String>> leadersMapInfo) {
        return String.format("Общее количество незаполненных отчетов: %s \n", size) + "\n" + leadersMapInfo;
    }

    private String groupsReportText(Integer openedGroups,
                                    Integer completedGroupsByMonth,
                                    Integer notCompleted,
                                    List<NotCompletedGroup> notCompletedGroups) {
        var title = String.format("Отчет по домашним группам за %s\n\n", getMonthName(LocalDate.now().getMonthValue() - 1, notCompletedGroups));
        return title +
               String.format("Открыто групп: %s\n", openedGroups) +
               String.format("Прошло групп: %s\n", completedGroupsByMonth) +
               String.format("Не прошло групп: %s\n\n", notCompleted) +
               notCompletedGroupsReportText(notCompletedGroups);
    }

    private String getMonthName(int number, List<NotCompletedGroup> notCompletedGroups) {
        if (notCompletedGroups.isEmpty()) {
            return "прошлую неделю";
        }
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

    private StringBuilder notCompletedGroupsReportText(List<NotCompletedGroup> notCompletedGroups) {
        if (notCompletedGroups.isEmpty()) {
            return new StringBuilder();
        }
        var sb = new StringBuilder();
        for (var notCompletedGroup : notCompletedGroups) {
            sb.append(String.format("%s, не прошла: %s раз(а)\n", notCompletedGroup.name(), notCompletedGroup.count()));
        }
        return sb;
    }

    private String getShortUrl(String leaderName, String date, Integer groupId) {
        var formUrl = YANDEX_FORM_URL + String.format("?name=%s&date=%s&groupId=%s", leaderName.replaceAll(" ", "%20"), date, groupId);
        return urlShorterClient.urlShorter(formUrl);
    }

}
