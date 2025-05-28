package org.example.fitpass.domain.point.repository;

import java.util.List;
import org.example.fitpass.domain.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    List<Point> findByUserIdOrderByCreatedAtDesc(Long userId);

}
