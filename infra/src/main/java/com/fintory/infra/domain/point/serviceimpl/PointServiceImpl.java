package com.fintory.infra.domain.point.serviceimpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.point.dto.PointWalletResponse;
import com.fintory.domain.point.model.PointTransaction;
import com.fintory.domain.point.model.PointTransactionSource;
import com.fintory.domain.point.model.PointWallet;
import com.fintory.domain.point.service.PointService;
import com.fintory.infra.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;

    @Override
    @Transactional
    public void givePointsByContinuousDays(int continuousDays, Child child) {
        int pointToGive = continuousDays * 10000;

        PointWallet pointWallet = pointRepository.findByChildId(child.getId())
                .orElseThrow(() -> new IllegalStateException("포인트 지갑이 없습니다. childId=" + child.getId()));

        updatePointAccount(pointWallet, pointToGive);
        createPointTransactions(pointWallet, pointToGive, PointTransactionSource.ATTENDANCE_POINT);
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
                .orElseThrow(() -> new IllegalStateException("포인트 지갑이 없습니다. childId=" + child.getId()));

        return PointWalletResponse.from(pointWallet);
    }

    // 포인트 지갑 업데이트
    // 부모 메소드에 transactional이 적용됐기에 해당 메소드로 적용이 됨
    // 따라서 pointWallet에 영속성이 적용되고 필드를 업데이트한 내용은 transactional 종료시 자동 commit 됨
    // -> 따라서 save() 호출 불필요
    private void updatePointAccount(PointWallet pointWallet, int point) {
        pointWallet.earnPoint(point);
    }

    // 포인트 거래내역 생성
    private void createPointTransactions(PointWallet pointWallet, int point, PointTransactionSource source) {
        PointTransaction pt = PointTransaction.earn(point, source, pointWallet);
        // wallet의 편의 메소드
        pointWallet.addTransaction(pt);
    }
}
