package org.wolrus.digital_system.model;


public record ReportRequest(
        Integer groupId,
        String name,
        String groupIsDone,
        Integer peopleCount,
        String evidence,
        String meetWithSenior,
        String date,
        String wishes
) {}
