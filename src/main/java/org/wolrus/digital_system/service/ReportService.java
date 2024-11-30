package org.wolrus.digital_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.wolrus.digital_system.entity.ReportEntity;
import org.wolrus.digital_system.model.ReportRequest;
import org.wolrus.digital_system.repository.GroupRepository;
import org.wolrus.digital_system.repository.ReportRepository;
import org.wolrus.digital_system.repository.UnfilledReportRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final GroupRepository groupRepository;
    private final ReportRepository reportRepository;
    private final UnfilledReportRepository unfilledReportRepository;

    public void createReport(ReportRequest request) {
        log.info("Получен отчет {}", request.toString());
        var groupId = request.groupId();
        var date = parseDate(request.date());
        if (reportRepository.findByGroupIdAndDate(groupId, date).isPresent()) {
            return;
        }
        var group = groupRepository.findById(request.groupId());
        var report = ReportEntity.of(request, date, group.orElse(null));
        reportRepository.saveAndFlush(report);
        var unfilledReport = unfilledReportRepository.findByLeaderNameAndReportDateAndGroup_Id(request.name(), date, request.groupId());
        unfilledReport.ifPresent(value -> {
            log.info("Найден отчет в базе не заполненных: {}", request);
            unfilledReportRepository.deleteById(value.getId());
        });
        unfilledReportRepository.flush();
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, FORMATTER);
        } catch (Exception e) {
            log.error("Ошибка при парсинге даты: {}", date, e);
        }
        return LocalDate.now();
    }
}
