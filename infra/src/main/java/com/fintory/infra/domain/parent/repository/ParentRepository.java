package com.fintory.infra.domain.parent.repository;

import com.fintory.domain.parent.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Parent> findByEmail(String email);

    Optional<Parent> findBySocialId(String socialId);
}
