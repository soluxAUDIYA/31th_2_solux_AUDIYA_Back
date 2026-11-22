package com.solux31th.audiya.domain.guardian.service;

import com.solux31th.audiya.domain.guardian.dto.GuardianReportResponse;
import com.solux31th.audiya.domain.guardian.entity.GuardianAccessToken;

public interface GuardianReportSource {
    ReportResult getReport(GuardianAccessToken token);

    enum Status { READY, PENDING, NOT_COMPLETED, NOT_FOUND }
    record ReportResult(Status status, GuardianReportResponse report) {}
}
