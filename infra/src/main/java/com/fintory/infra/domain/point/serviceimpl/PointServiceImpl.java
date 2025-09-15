package com.fintory.infra.domain.point.serviceimpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.account.model.Account;
import com.fintory.domain.account.model.DepositTransaction;
import com.fintory.domain.account.model.DepositTransactionType;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.point.dto.ExchangedCashResponse;
import com.fintory.domain.point.dto.PointWalletResponse;
import com.fintory.domain.point.model.PointTransaction;
import com.fintory.domain.point.model.PointTransactionSource;
import com.fintory.domain.point.model.PointWallet;
import com.fintory.domain.point.service.PointService;
import com.fintory.infra.domain.account.repository.AccountRepository;
import com.fintory.infra.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public void givePointsByContinuousDays(int continuousDays, Child child) {
        int pointToGive = continuousDays * 10000;

        PointWallet pointWallet = pointRepository.findByChildId(child.getId())
                .orElseThrow(() -> new IllegalStateException("포인트 지갑이 없습니다. childId=" + child.getId()));

        updateEarningPointWallet(pointWallet, pointToGive);
        createEarningPointTransactions(pointWallet, pointToGive, PointTransactionSource.ATTENDANCE_POINT);
    }

    @Override
    public void createInitialPointWallet(Child child) {
        try {
            PointWallet wallet = PointWallet.createWithInitialPointWallet(child);
            pointRepository.save(wallet);
            log.info("포인트 지갑 생성 완료: childId= {}", child.getId());
        } catch (Exception e) {
            throw new DomainException(DomainErrorCode.INITIALIZE_POINT_WALLET_FAILED, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PointWalletResponse getPointWalletWithTransactions(Child child) {

        PointWallet pointWallet = pointRepository.findByChildId(child.getId())
                .orElseThrow(() -> new DomainException(DomainErrorCode.INITIALIZE_POINT_WALLET_FAILED));

        return PointWalletResponse.from(pointWallet);
    }

    @Override
    public int getTotalAmount(Child child) {

        PointWallet wallet = pointRepository.findByChildId(child.getId())
                .orElseThrow(() -> new DomainException(DomainErrorCode.INITIALIZE_POINT_WALLET_FAILED));
        return wallet.getTotalAmount();
    }

    @Override
    @Transactional
    public ExchangedCashResponse exchangePoint(Child child, int pointAmount) {
        PointWallet wallet = pointRepository.findByChildId(child.getId())
                .orElseThrow(() -> new DomainException(DomainErrorCode.POINT_WALLET_NOT_FOUND));

        if (wallet.getTotalAmount() < pointAmount) { // 보유 총 포인트 < 환전신청포인트
            throw new DomainException(DomainErrorCode.POINT_INSUFFICIENT);
        }

        Account account = accountRepository.findByChild(child)
                .orElseThrow(() -> new DomainException(DomainErrorCode.ACCOUNT_NOT_FOUND));

        // 포인트계좌 업데이트
        updateWithdrawPointWallet(wallet, pointAmount);
        // 포인트 거래내역 생성
        createExchangedPointTransactions(wallet, pointAmount);

        // 현금 거래내역 생성
        DepositTransaction deposit = DepositTransaction.create(BigDecimal.valueOf(pointAmount), "포인트 환전", DepositTransactionType.EXCHANGE);
        //현금 계좌 업데이트(연관관계 설정)
        account.addDepositTransaction(deposit);

        return new ExchangedCashResponse(pointAmount);
    }

    // 포인트 지갑 업데이트
    // 부모 메소드에 transactional이 적용됐기에 해당 메소드로 적용이 됨
    // 따라서 pointWallet에 영속성이 적용되고 필드를 업데이트한 내용은 transactional 종료시 자동 commit 됨
    // -> 따라서 save() 호출 불필요
    private void updateEarningPointWallet(PointWallet pointWallet, int point) {
        pointWallet.earnPoint(point);
    }

    private void updateWithdrawPointWallet(PointWallet pointWallet, int point) {
        pointWallet.withdrawPoint(point);
    }

    // 포인트 거래내역 생성, 지갑과 연결
    private void createEarningPointTransactions(PointWallet pointWallet, int point, PointTransactionSource source) {
        PointTransaction pt = PointTransaction.createEarningTransaction(point, source, pointWallet);
        // wallet의 편의 메소드
        pointWallet.addTransaction(pt);
    }

    private void createExchangedPointTransactions(PointWallet pointWallet, int point) {
        PointTransaction pt = PointTransaction.createExchangeTransaction(point, pointWallet);
        // wallet의 편의 메소드
        pointWallet.addTransaction(pt);
    }

}
