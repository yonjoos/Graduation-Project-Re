package PickMe.PickMeDemo.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 쿼리DSL을 프로젝트 스프링 빈으로 등록해주는 곳
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    public QueryDslConfig() {
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(this.entityManager);
    }
}
