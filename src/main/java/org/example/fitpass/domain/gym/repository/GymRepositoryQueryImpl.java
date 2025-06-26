package org.example.fitpass.domain.gym.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.entity.QGym;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class GymRepositoryQueryImpl implements GymRepositoryQuery {
    private final JPAQueryFactory queryFactory;  // QueryDSL JPAQueryFactory 주입

    @Override
    public Page<Gym> searchGym(String keyword, String city, String district, Pageable pageable) {

        if ("undefined".equalsIgnoreCase(keyword)) keyword = null;
        if ("undefined".equalsIgnoreCase(city)) city = null;
        if ("undefined".equalsIgnoreCase(district)) district = null;

        QGym gym = QGym.gym;

        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.isEmpty()) {
            builder.and(gym.name.containsIgnoreCase(keyword));
        }
        if (city != null && !city.isBlank()) {
            if (city.endsWith("도") || city.endsWith("시")) {
                city = city.substring(0, 2); // 예: "경기도" → "경기", "서울특별시" → "서울"
            }
            builder.and(gym.city.containsIgnoreCase(city));
        }
        if (district != null && !district.isEmpty()) {
            builder.and(gym.district.containsIgnoreCase(district));
        }

        // QueryDSL로 실제 쿼리 작성
        List<Gym> results = queryFactory.selectFrom(gym)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory.selectFrom(gym)
            .where(builder)
            .fetchCount();

        return new PageImpl<>(results, pageable, total);
    }
}
