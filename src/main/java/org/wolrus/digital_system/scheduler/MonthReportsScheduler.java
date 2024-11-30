package org.wolrus.digital_system.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wolrus.digital_system.repository.ReportRepository;
import org.wolrus.digital_system.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthReportsScheduler {

    @Value("${telegram.pastor-id}")
    private String PASTOR_TELEGRAM_ID;

    private final ReportRepository reportRepository;
    private final NotificationService notificationService;

    @Transactional
    @Scheduled(cron = "0 0 12 1 * ?", zone = "Europe/Moscow")
    public void report() {
        var sb = new StringBuilder();
        sb.append("<b>Месячный отчет по РЛ</b>");
        sb.append("<pre>");
        sb.append(String.format("%-12s %-4s %-3s %-5s %-4s\n\n",
                "Имя", "Мес", "Пр", "Не пр", "Людей"));
        var dataForPastor = reportRepository.reportForPastor();
        for (var report : dataForPastor) {
            sb.append(String.format("%-12s %-4s %-3d %-5d %-4d\n",
                    report.getRegionalLeaderName(),
                    getMonth(report.getMonth()),
                    report.getIsDone(),
                    report.getIsNotDone(),
                    report.getPersonCount()));
        }
        sb.append("</pre>");
        notificationService.sendNotification(PASTOR_TELEGRAM_ID, sb.toString());
    }

    private String getMonth(String englishName) {
        return switch (englishName) {
            case "Sep" -> "Сен";
            case "Oct" -> "Окт";
            case "Nov" -> "Ноя";
            default -> "Не определен";
        };
    }

}
