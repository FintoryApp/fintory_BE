package com.fintory.infra.domain.account.serviceImpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.account.dto.response.DepositTransactionResponse;
import com.fintory.domain.account.dto.response.TotalAssetsResponse;
import com.fintory.domain.account.model.Account;
import com.fintory.domain.account.model.DepositTransaction;
import com.fintory.domain.account.service.AccountService;
import com.fintory.domain.child.model.Child;
import com.fintory.infra.domain.account.repository.AccountRepository;
import com.fintory.infra.domain.account.repository.DepositTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final DepositTransactionRepository depositTransactionRepository;

    @Override
    public void createInitialAccount(Child child) {
        BigDecimal initialCash = new BigDecimal("100000");
        try {
            Account account = Account.createWithInitialDeposit(child, initialCash);
            accountRepository.save(account);
            log.info("계좌 생성 완료: childId={}, 초기 입금={}", child.getId(), initialCash);
        } catch (Exception e) {
            throw new DomainException(DomainErrorCode.INITIALIZE_ACCOUNT_FAILED, e);
        }
    }

    @Override
    public TotalAssetsResponse getTotalAssets(Child child) {
        Account account = accountRepository.findByChild(child)
                .orElseThrow(() -> new DomainException(DomainErrorCode.ACCOUNT_NOT_FOUND));

        return TotalAssetsResponse.from(account);
    }

    @Transactional(readOnly = true)
    public List<DepositTransactionResponse> getDepositTransactionsByChild(Child child) {
        Account account = accountRepository.findByChild(child)
                .orElseThrow(() -> new DomainException(DomainErrorCode.ACCOUNT_NOT_FOUND));

        List<DepositTransaction> transactions =
                depositTransactionRepository.findByAccountOrderByOccurredAtDesc(account);

        return transactions.stream()
                .map(DepositTransactionResponse::from)
                .toList();
    }
}
