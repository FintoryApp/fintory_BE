package com.fintory.infra.domain.parent.repository;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.parent.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Child> findByEmail(String email);

    Optional<Child> findBySocialId(String socialId);
}
