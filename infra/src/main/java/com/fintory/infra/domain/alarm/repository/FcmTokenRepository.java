package com.fintory.infra.domain.alarm.repository;

import com.fintory.domain.alarm.model.FcmToken;
import com.fintory.domain.child.model.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, String> {
    List<FcmToken> findByChild(Child child);

    boolean existsByToken(String token);

    void deleteByToken(String token);
}
