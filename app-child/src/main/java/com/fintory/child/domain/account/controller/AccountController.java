package com.fintory.child.domain.account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "계좌 API")
public interface AccountController {

    // TODO: 입력 금액 입금(depositTransaction 내역에 반영)



    // TODO: 포인트 환전 내역 가져오기 + 주식 매매 내역 가져오기 (합쳐서 가져오고 정렬 시간순으로 응답)
        // TODO: 포인트 엔티티, pointTransaction 정의
        // TODO: 페이징 처리

    // TODO: 총 재산 가져오기 -> 총 현금이랑 (주식 종목 + 개수)만 응답하고 프론트에서 현재가 받아서 총재산 도출

}
