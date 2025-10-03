package com.fintory.infra.domain.point.serviceimpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.account.model.Account;
import com.fintory.domain.account.model.DepositTransaction;
import com.fintory.domain.account.model.DepositTransactionType;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.point.dto.PointWalletResponse;
import com.fintory.domain.point.model.PointTransaction;
import com.fintory.domain.point.model.PointTransactionSource;
import com.fintory.domain.point.model.PointWallet;
import com.fintory.domain.point.service.PointService;
import com.fintory.infra.domain.account.repository.AccountRepository;
import com.fintory.infra.domain.account.repository.DepositTransactionRepository;
import com.fintory.infra.domain.point.repository.PointRepository;
import com.fintory.infra.domain.point.repository.PointTransactionRepository;
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
    private final PointTransactionRepository pointTransactionRepository;
    private final AccountRepository accountRepository;
    private final DepositTransactionRepository depositTransactionRepository;

    @Override
    @Transactional
    public void givePointsByContinuousDays(int continuousDays, Child child) {
        int pointToGive = continuousDays * 10000;

        PointWallet pointWallet = pointRepository.findByChildId(child.getId())
                .orElseThrow(() -> new IllegalStateException("포인트 지갑이 없습니다. childId=" + child.getId()));

        // setter
        updatePointAccount(pointWallet, pointToGive);
        // 정적 팩토리 메소드
        PointTransaction pointTransaction = PointTransaction.createEarningTransaction(pointToGive, PointTransactionSource.ATTENDANCE_POINT, pointWallet);
        pointTransactionRepository.save(pointTransaction); // 영속성 전이 이용하지 않고 명시적으로 저장.

    }

    @Override
    @Transactional
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
    public Integer exchangePointsToCash(Child child, int point) {

        PointWallet pointWallet = pointRepository.findByChildId(child.getId())
                .orElseThrow(() -> new IllegalStateException("포인트 지갑이 없습니다. childId=" + child.getId()));

        Account account = accountRepository.findByChildId(child.getId())
                .orElseThrow(()-> new DomainException(DomainErrorCode.ACCOUNT_NOT_FOUND));

        if (pointWallet.getTotalAmount() < point) {
            throw new DomainException(DomainErrorCode.NOT_ENOUGH_POINT);
        }

        //포인트 지갑 업데이트 -> setter
        updatePointAccountByExchange(pointWallet, point);
        //포인트 내역 생성 -> 정적 팩토리 메소드
        PointTransaction pointTransaction = PointTransaction.createWithdrawTransaction(point, PointTransactionSource.EXCHANGE_POINT, pointWallet);
        pointTransactionRepository.save(pointTransaction);
        //현금 지갑 업데이트, 보유 현금 반환 -> setter
        BigDecimal availableCash = updateAccountByExchange(account, BigDecimal.valueOf(point));
        //현금 내역 생성 -> 정적 팩토리 메소드
        DepositTransaction depositTransaction = DepositTransaction.create(BigDecimal.valueOf(point), "포인트 환전", DepositTransactionType.DEPOSIT, account);
        depositTransactionRepository.save(depositTransaction);

        return availableCash.intValue();
    }

    // 부모 메소드에 transactional이 적용됐기에 해당 메소드로 적용이 됨
    // 따라서 pointWallet에 영속성이 적용되고 필드를 업데이트한 내용은 transactional 종료시 자동 commit 됨
    // -> 따라서 save() 호출 불필요
    private void updatePointAccount(PointWallet pointWallet, int point) {
        pointWallet.earnPoint(point);
    }

    private void updatePointAccountByExchange(PointWallet pointWallet, int point) {
        pointWallet.withdrawPoint(point);
    }

    private BigDecimal updateAccountByExchange(Account account, BigDecimal exchangePoint) {
        account.updateExchangePoint(exchangePoint);
        return account.getAvailableCash();
    }
}
