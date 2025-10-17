package com.fintory.child.domain.consulting.controller;


import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.child.service.ChildService;
import com.fintory.domain.consulting.dto.ReportDetail;
import com.fintory.domain.consulting.service.ConsultingService;
import com.fintory.infra.domain.consulting.serviceImpl.ConsultingSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/child/consulting")
public class ConsultingControllerImpl implements ConsultingController{

    private final ConsultingService consultingService;
    private final ConsultingSchedulerService schedulerService;
    private final ChildService childService;

    //테스트용
    @PostMapping("/test/consulting-report")
    public void triggerConsultingReport() {
         schedulerService.scheduledGenerateConsultingReport();
    }

    // 날짜별 리포트 조회
    @GetMapping("/{reportMonth}")
    public ResponseEntity<ApiResponse<ReportDetail>> getConsultingByDate(@PathVariable String reportMonth,  @AuthenticationPrincipal CustomUserDetails user){
        Child child = childService.getChild(user.getUsername());
        ReportDetail reportDetail =  consultingService.getConsultingByDate(reportMonth,child);
        return ResponseEntity.ok(ApiResponse.ok(reportDetail));
    }

}
