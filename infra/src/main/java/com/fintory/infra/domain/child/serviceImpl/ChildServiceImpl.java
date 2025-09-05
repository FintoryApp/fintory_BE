package com.fintory.infra.domain.child.serviceImpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.child.service.ChildService;
import com.fintory.infra.domain.child.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChildServiceImpl implements ChildService {

    private final ChildRepository childRepository;

    @Override
    public Child getChild(String email) {
        return childRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException(DomainErrorCode.USER_NOT_FOUND));
    }
}
