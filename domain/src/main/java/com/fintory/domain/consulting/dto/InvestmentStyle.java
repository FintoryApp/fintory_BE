package com.fintory.domain.consulting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentStyle {
    private Long childId;
    private String childName;
    private String investmentStyle;
}
