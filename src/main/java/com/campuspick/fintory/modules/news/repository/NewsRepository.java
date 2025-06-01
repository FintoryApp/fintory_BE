package com.campuspick.fintory.modules.news.repository;


import com.campuspick.fintory.modules.news.domain.entity.News;
import com.campuspick.fintory.modules.news.repository.querydsl.NewsRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Long>, NewsRepositoryCustom {
}
