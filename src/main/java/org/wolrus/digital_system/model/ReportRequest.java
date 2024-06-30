package org.wolrus.digital_system.model;


public record ReportRequest(
        String name,
        String groupIsDone,
        Integer peopleCount,
        String evidence,
        String meetWithSenior,
        String date
) {}
