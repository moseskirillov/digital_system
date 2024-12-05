package org.wolrus.digital_system.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wolrus.digital_system.repository.GroupLeaderRepository;
import org.wolrus.digital_system.repository.RegionalLeaderRepository;
import org.wolrus.digital_system.repository.ReportRepository;
import org.wolrus.digital_system.service.NotificationService;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthReportsScheduler {

    private static final String HOME_GROUP_TYPE = "Взрослые";
    private static final Locale LOCALE = Locale.of("ru");

    @Value("${telegram.pastor-id}")
    private String PASTOR_TELEGRAM_ID;

    @Value("${telegram.groups-admin-id}")
    private String GROUPS_ADMIN_TELEGRAM_ID;

    @Value("${data.region-leader-for-ignore.young}")
    private Integer REGION_LEADER_ID_FOR_IGNORE_YOUNG;

    private final ReportRepository reportRepository;
    private final NotificationService notificationService;
    private final GroupLeaderRepository groupLeaderRepository;
    private final RegionalLeaderRepository regionalLeaderRepository;

    @Transactional
    @Scheduled(cron = "0 0 12 1 * ?", zone = "Europe/Moscow")
    public void pastorNotifier() {
        var sb = new StringBuilder();
        var month = LocalDate.now().getMonth().getValue() - 1;
        var monthTitle = Month.of((month == 0) ? 12 : month).getDisplayName(TextStyle.FULL_STANDALONE, LOCALE);
        sb.append(String.format("<b>Отчет за %s</b><pre>", monthTitle));
        sb.append(String.format("%-12s %-7s %-9s %-8s\n\n", "Имя", "Прошло", "Не прошло", "Людей"));
        var dataForPastor = reportRepository.reportForPastor();
        for (var report : dataForPastor) {
            sb.append(String.format("%-12s %-7d %-9d %-8d\n",
                    report.getRegionalLeaderName(),
                    report.getIsDone(),
                    report.getIsNotDone(),
                    report.getPersonCount()));
        }
        sb.append("</pre>");
        notificationService.sendNotification(PASTOR_TELEGRAM_ID, sb.toString());
        notificationService.sendNotification(GROUPS_ADMIN_TELEGRAM_ID, sb.toString());
    }

    @Transactional
    @Scheduled(cron = "0 0 12 1 * ?", zone = "Europe/Moscow")
    public void regionalLeaderNotifier() {
        var ignoreIds = List.of(REGION_LEADER_ID_FOR_IGNORE_YOUNG);
        var regionalLeaders = regionalLeaderRepository.findAllByIdNotIn(ignoreIds);
        for (var regionalLeader : regionalLeaders) {
            var dataForRegionalLeader = reportRepository.reportForRegionalLeader(regionalLeader.getId());
            if (dataForRegionalLeader.isEmpty()) {
                continue;
            }
            var sb = new StringBuilder();
            var user = regionalLeader.getUser();
            var month = LocalDate.now().getMonth().getValue() - 1;
            var monthTitle = Month.of((month == 0) ? 12 : month).getDisplayName(TextStyle.FULL_STANDALONE, LOCALE);
            sb.append(String.format("<b>Отчет по региону за %s\nЛидер %s</b>", monthTitle, user.getFullName()))
                    .append("<pre>");
            sb.append(String.format("%-14s %-7s %-9s %-8s\n\n", "Имя", "Прошло", "Не прошло", "Людей"));
            for (var report : dataForRegionalLeader) {
                sb.append(String.format("%-14s %-7d %-9d %-8d\n",
                        report.getLeaderName(),
                        report.getIsDone(),
                        report.getIsNotDone(),
                        report.getPersonCount()));
            }
            sb.append("</pre>");
            notificationService.sendNotification(user.getTelegramId(), sb.toString());
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 12 1 * ?", zone = "Europe/Moscow")
    public void groupLeaderNotifier() {
        var groups = groupLeaderRepository.findAllByGroups_IsOpenAndGroups_Age(true, HOME_GROUP_TYPE);
        for (var group : groups) {
            var user = group.getUser();
            var fullName = user.getFullName();
            var report = reportRepository.reportForGroupLeader(fullName);
            if (report == null || report.getPersonCount() == null || report.getIsDone() == null || report.getIsNotDone() == null) {
                continue;
            }
            var sb = new StringBuilder();
            var month = LocalDate.now().getMonth().getValue() - 1;
            var monthTitle = Month.of((month == 0) ? 12 : month).getDisplayName(TextStyle.FULL_STANDALONE, LOCALE);
            sb.append(String.format("<b>Отчет за %s\nЛидер %s</b>", monthTitle, fullName)).append("<pre>");
            sb.append(String.format("%-12s %-12s %-10s\n\n", "Прошло", "Не прошло", "Людей"));
            sb.append(String.format("%-12d %-12d %-10d\n",
                    report.getIsDone(),
                    report.getIsNotDone(),
                    report.getPersonCount())
            );
            sb.append("</pre>");
            notificationService.sendNotification(user.getTelegramId(), sb.toString());
        }
    }

}
