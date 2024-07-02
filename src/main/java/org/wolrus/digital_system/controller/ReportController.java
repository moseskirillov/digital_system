package org.wolrus.digital_system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wolrus.digital_system.model.ReportRequest;
import org.wolrus.digital_system.scheduled.ScheduledComponent;
import org.wolrus.digital_system.service.ReportService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ScheduledComponent scheduledComponent;

    @PostMapping("/create")
    public void createReport(@RequestBody ReportRequest request) {
        reportService.createReport(request);
    }

    @GetMapping("/notify")
    public void startNotifyProcess() {
        scheduledComponent.notifyLeadersScheduler();
    }

}
