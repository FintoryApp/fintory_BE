package com.fintory.infra.domain.account.serviceImpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.account.dto.response.DepositTransactionResponse;
import com.fintory.domain.account.model.Account;
import com.fintory.domain.account.model.DepositTransaction;
import com.fintory.domain.account.model.DepositTransactionType;
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
    @Transactional
    public void createInitialAccount(Child child) {

        BigDecimal initialCash = new BigDecimal("100000");

        try {
            //계좌 생성
            Account account = Account.create(child, initialCash);
            accountRepository.save(account);

            //초기금 입금 거래내역 생성
            DepositTransaction deposit = DepositTransaction.create(initialCash, "기본금 지급", DepositTransactionType.DEPOSIT, account);
            depositTransactionRepository.save(deposit);

        } catch (Exception e) {
            throw new DomainException(DomainErrorCode.INITIALIZE_ACCOUNT_FAILED, e);
        }
    }

    @Override
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

    @Override
    public BigDecimal getTotalCashByChild(Child child) {
        Account account = accountRepository.findByChild(child)
                .orElseThrow(() -> new DomainException(DomainErrorCode.ACCOUNT_NOT_FOUND));

        return account.getAvailableCash();
    }
}
