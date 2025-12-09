package com.zerooneblog.api.interfaces.dto.requestDto;

import lombok.Data;
import com.zerooneblog.api.domain.ReportReason;

@Data
public class UserReportRequest {
    private ReportReason reason;
    private String details;
}
