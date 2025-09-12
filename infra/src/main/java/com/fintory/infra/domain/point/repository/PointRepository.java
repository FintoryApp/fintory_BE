package com.fintory.infra.domain.point.repository;

import com.fintory.domain.point.model.PointWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository<PointWallet, Long> {

    Optional<PointWallet> findByChildId(long childId);
//
//    @Query("""
//        select pw from PointWallet pw
//        join fetch pw.transactions
//        where pw.child.id = :childId
//    """)
//    Optional<PointWallet> findByChildIdWithTransactions(@Param("childId") Long childId);
}
