package com.fintory.child.consulting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentStyle {
    private String childId;
    private String childName;
    private String investmentStyle;
}
