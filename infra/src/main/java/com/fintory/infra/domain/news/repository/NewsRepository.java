package com.fintory.infra.domain.news.repository;

import com.fintory.domain.news.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {

    @Modifying
    @Query("DELETE FROM News") // JPQL 사용: NewsArticle 엔티티 전체 삭제
    void deleteAllNewsArticles();

    List<News> findAllByOrderByPublishedAtDesc();

    Optional<News> findById(Long id);

}
