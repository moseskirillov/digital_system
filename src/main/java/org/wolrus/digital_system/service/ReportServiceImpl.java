package org.wolrus.digital_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.wolrus.digital_system.entity.Report;
import org.wolrus.digital_system.model.ReportRequest;
import org.wolrus.digital_system.repository.ReportRepository;
import org.wolrus.digital_system.repository.UnfilledReportRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final ReportRepository reportRepository;
    private final UnfilledReportRepository unfilledReportRepository;

    @Override
    public void createReport(ReportRequest request) {
        log.info("Получен отчет {}", request.toString());
        var report = Report.of(request);
        reportRepository.saveAndFlush(report);
        var date = parseDate(request.date());
        var unfilledReport = unfilledReportRepository.findByLeaderNameAndReportDate(request.name(), date);
        unfilledReport.ifPresent(value -> {
            log.info("Найден отчет в базе не заполненных: {}", request);
            unfilledReportRepository.deleteById(value.getId());
        });
        unfilledReportRepository.flush();
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, FORMATTER);
        } catch (Exception e)  {
            log.error("Ошибка при парсинге даты: {}", date, e);
        }
        return LocalDate.now();
    }
}
