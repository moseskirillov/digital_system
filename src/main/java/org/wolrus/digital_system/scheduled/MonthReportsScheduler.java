package org.wolrus.digital_system.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wolrus.digital_system.entity.UnfilledReport;
import org.wolrus.digital_system.entity.v2.GroupLeaderEntity;
import org.wolrus.digital_system.entity.v2.RegionalLeaderEntity;
import org.wolrus.digital_system.entity.v2.UserEntity;
import org.wolrus.digital_system.model.LeaderInfo;
import org.wolrus.digital_system.repository.UnfilledReportRepository;
import org.wolrus.digital_system.repository.v2.GroupLeaderRepository;
import org.wolrus.digital_system.repository.v2.RegionalLeaderRepository;
import org.wolrus.digital_system.service.NotificationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthReportsScheduler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Value("${telegram.groups-admin-id}")
    private String GROUPS_ADMIN_TELEGRAM_ID;

    @Value("${telegram.pastor-id}")
    private String PASTOR_TELEGRAM_ID;

    private final NotificationService notificationService;
    private final GroupLeaderRepository groupLeaderRepository;
    private final UnfilledReportRepository unfilledReportRepository;
    private final RegionalLeaderRepository regionalLeaderRepository;

    @Scheduled(cron = "0 0 19 * * MON", zone = "Europe/Moscow")
    public void unfilledReportsNotifier() {
        log.info("Запуск рассылки информации о незаполненных отчетах");
        var now = LocalDate.now().minusDays(2);
        var unfilledReports = unfilledReportRepository.findAllByDate(now);
        if (unfilledReports.isEmpty()) {
            log.info("Нет незаполненных отчетов");
            var regionalLeaders = regionalLeaderRepository.findAllByUser_TelegramIdNotNull().stream()
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
            sb.append("Здравствуйте \uD83D\uDE07 \n");
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

    private void sendMessageAboutNotUnfilledReports(List<String> regionalLeaders) {
        for (var rl : regionalLeaders) {
            notificationService.sendNotification(rl,
                    """
                            Здравствуйте
                            В вашем регионе нет задолженностей по отчетам \uD83D\uDE07
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

    private Map<LeaderInfo, List<String>> createLeadersInfoMap(List<UnfilledReport> unfilledReports) {
        var leadersMapInfo = createLeadersMapInfo();
        for (var unfilledReport : unfilledReports) {
            var groupLeaderEntity = groupLeaderRepository.findById(unfilledReport.getLeaderId());
            var leaderName = groupLeaderEntity.map(GroupLeaderEntity::getUser)
                    .map(e -> e.getLastName() + " " + e.getFirstName());
            var regionalLeaderModel = groupLeaderEntity.map(GroupLeaderEntity::getRegionalLeaderEntity)
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

}
