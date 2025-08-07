package com.fintory.infra.domain.child.repository;

import com.fintory.domain.child.model.Child;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChildRepository extends JpaRepository<Child, Long> {

    Optional<Child> findByEmail(String loginId);
}
