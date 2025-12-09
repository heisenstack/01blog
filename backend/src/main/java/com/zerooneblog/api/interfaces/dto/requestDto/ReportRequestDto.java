package com.zerooneblog.api.interfaces.dto.requestDto;

import com.zerooneblog.api.domain.ReportReason;

import lombok.Data;

@Data
public class ReportRequestDto {
    private ReportReason reason;
    private String detials;
}
