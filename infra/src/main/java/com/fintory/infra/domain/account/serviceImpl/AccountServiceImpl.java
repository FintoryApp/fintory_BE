package com.fintory.infra.domain.account.serviceImpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.account.model.Account;
import com.fintory.domain.account.service.AccountService;
import com.fintory.domain.child.model.Child;
import com.fintory.infra.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

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
}
