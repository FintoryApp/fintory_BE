package com.fintory.infra.domain.word.repository;

import com.fintory.domain.financialword.model.FinancialWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<FinancialWord, Long> {

    Optional<FinancialWord> findFirstByWord(String word);

    @Query(value = "SELECT * FROM Financial_word ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<FinancialWord> wordRepositoryRandomWord();

    @Query("SELECT f FROM FinancialWord f WHERE f.word LIKE %:keyword%")
    List<FinancialWord> searchByKeyword(@Param("keyword") String keyword);
}
