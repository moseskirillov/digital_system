package org.wolrus.digital_system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wolrus.digital_system.model.ReportRequest;
import org.wolrus.digital_system.service.ReportService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/create")
    public void createReport(@RequestBody ReportRequest request) {
        reportService.createReport(request);
    }

}
