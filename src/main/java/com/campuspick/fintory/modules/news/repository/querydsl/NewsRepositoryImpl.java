package com.campuspick.fintory.modules.news.repository.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NewsRepositoryImpl implements NewsRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    //override
}
